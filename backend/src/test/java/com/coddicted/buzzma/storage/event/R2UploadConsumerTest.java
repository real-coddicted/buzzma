package com.coddicted.buzzma.storage.event;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.coddicted.buzzma.claim.service.ClaimService;
import com.coddicted.buzzma.storage.config.R2Properties;
import com.coddicted.buzzma.storage.service.StorageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@ExtendWith(MockitoExtension.class)
class R2UploadConsumerTest {

  private static final UUID SCREENSHOT_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
  private static final UUID CLAIM_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
  private static final String STORAGE_KEY = "claims/abc123.jpg";
  private static final String CAMPAIGN_CODE = "CAM001";
  private static final String CLAIM_CODE = "CLM001";
  private static final String PUBLIC_URL_BASE = "https://cdn.example.com";
  private static final byte[] FILE_BYTES = {1, 2, 3, 4, 5};

  @Mock private StringRedisTemplate mockRedisTemplate;
  @Mock private ClaimService mockClaimService;
  @Mock private StorageService mockStorageService;
  @Mock private S3Client mockR2Client;

  private R2UploadConsumer consumer;

  @BeforeEach
  void setUp() {
    final R2Properties properties = new R2Properties();
    properties.setEnabled(true);
    properties.setBucket("test-bucket");
    properties.setPublicUrlBase(PUBLIC_URL_BASE);
    this.consumer =
        new R2UploadConsumer(
            this.mockRedisTemplate,
            new ObjectMapper(),
            this.mockClaimService,
            this.mockStorageService,
            this.mockR2Client,
            properties);
  }

  @Test
  void uploadScreenshotToR2_uploadsAndUpdatesDb() throws IOException {
    final R2UploadMessage message =
        new R2UploadMessage(
            SCREENSHOT_ID,
            CLAIM_ID,
            CAMPAIGN_CODE,
            CLAIM_CODE,
            STORAGE_KEY,
            "SCREENSHOT_TYPE_ORDER");

    final GetObjectResponse objectResponse =
        GetObjectResponse.builder().contentType("image/jpeg").build();
    @SuppressWarnings("unchecked")
    final ResponseBytes<GetObjectResponse> responseBytes =
        (ResponseBytes<GetObjectResponse>) ResponseBytes.fromByteArray(objectResponse, FILE_BYTES);

    when(this.mockStorageService.retrieve(STORAGE_KEY)).thenReturn(responseBytes);

    this.consumer.uploadScreenshotToR2(message);

    final ArgumentCaptor<PutObjectRequest> requestCaptor =
        ArgumentCaptor.forClass(PutObjectRequest.class);
    final ArgumentCaptor<RequestBody> bodyCaptor = ArgumentCaptor.forClass(RequestBody.class);
    verify(this.mockR2Client).putObject(requestCaptor.capture(), bodyCaptor.capture());

    final PutObjectRequest captured = requestCaptor.getValue();
    assertEquals("test-bucket", captured.bucket());
    assertEquals("CAM001/CLM001/screenshot_type_order.jpg", captured.key());
    assertEquals("image/jpeg", captured.contentType());
    assertArrayEquals(
        FILE_BYTES, bodyCaptor.getValue().contentStreamProvider().newStream().readAllBytes());

    verify(this.mockClaimService)
        .updateScreenshotPublicUrl(
            SCREENSHOT_ID, "https://cdn.example.com/CAM001/CLM001/screenshot_type_order.jpg");
  }
}
