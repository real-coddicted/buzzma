package com.coddicted.buzzma.storage.service.impl;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.coddicted.buzzma.shared.exception.NotFoundException;
import com.coddicted.buzzma.storage.config.R2Properties;
import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@ExtendWith(MockitoExtension.class)
class R2StorageServiceImplTest {

  private static final String BUCKET = "test-bucket";
  private static final String FOLDER = "CAM001/CLM001";
  private static final String FILENAME = "abc-screenshot_type_order.jpg";
  private static final String KEY = FOLDER + "/" + FILENAME;
  private static final byte[] DATA = {1, 2, 3};

  @Mock private S3Client mockR2Client;

  private R2StorageServiceImpl storageService;

  @BeforeEach
  void setUp() {
    final R2Properties properties = new R2Properties();
    properties.setBucket(BUCKET);
    this.storageService = new R2StorageServiceImpl(this.mockR2Client, properties);
  }

  @Test
  void store_putsObjectUnderFolderAndFilenameAsGiven() throws IOException {
    final String key = this.storageService.store(FOLDER, FILENAME, "image/jpeg", DATA);

    final ArgumentCaptor<PutObjectRequest> requestCaptor =
        ArgumentCaptor.forClass(PutObjectRequest.class);
    final ArgumentCaptor<RequestBody> bodyCaptor = ArgumentCaptor.forClass(RequestBody.class);
    verify(this.mockR2Client).putObject(requestCaptor.capture(), bodyCaptor.capture());

    assertEquals(KEY, key);
    assertEquals(
        PutObjectRequest.builder().bucket(BUCKET).key(KEY).contentType("image/jpeg").build(),
        requestCaptor.getValue());
    assertArrayEquals(
        DATA, bodyCaptor.getValue().contentStreamProvider().newStream().readAllBytes());
  }

  @Test
  @SuppressWarnings("unchecked")
  void retrieve_missingKeyThrowsNotFound() {
    final GetObjectRequest request = GetObjectRequest.builder().bucket(BUCKET).key(KEY).build();
    final ArgumentCaptor<ResponseTransformer<GetObjectResponse, Object>> transformerCaptor =
        ArgumentCaptor.forClass(ResponseTransformer.class);
    when(this.mockR2Client.getObject(eq(request), transformerCaptor.capture()))
        .thenThrow(NoSuchKeyException.builder().build());

    assertThrows(NotFoundException.class, () -> this.storageService.retrieve(KEY));
  }

  @Test
  void delete_deletesObjectByKey() {
    this.storageService.delete(KEY);

    verify(this.mockR2Client)
        .deleteObject(DeleteObjectRequest.builder().bucket(BUCKET).key(KEY).build());
  }
}
