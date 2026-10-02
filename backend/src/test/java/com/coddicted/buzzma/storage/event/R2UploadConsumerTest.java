package com.coddicted.buzzma.storage.event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.coddicted.buzzma.claim.service.ClaimService;
import com.coddicted.buzzma.storage.config.R2Properties;
import com.coddicted.buzzma.storage.service.StorageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

@ExtendWith(MockitoExtension.class)
class R2UploadConsumerTest {

  private static final UUID SCREENSHOT_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
  private static final UUID CLAIM_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
  private static final String STORAGE_KEY = "claims/abc123.jpg";
  private static final String CAMPAIGN_CODE = "CAM001";
  private static final String CLAIM_CODE = "CLM001";
  private static final String PUBLIC_URL_BASE = "https://cdn.example.com";
  private static final byte[] FILE_BYTES = {1, 2, 3, 4, 5};
  private static final String R2_FILENAME =
      "11111111-1111-1111-1111-111111111111-screenshot_type_order.jpg";

  @Mock private StringRedisTemplate mockRedisTemplate;
  @Mock private ClaimService mockClaimService;
  @Mock private StorageService mockStorageService;
  @Mock private StorageService mockR2StorageService;

  private static final R2UploadMessage MESSAGE =
      new R2UploadMessage(
          SCREENSHOT_ID, CLAIM_ID, CAMPAIGN_CODE, CLAIM_CODE, STORAGE_KEY, "SCREENSHOT_TYPE_ORDER");

  private R2UploadConsumer consumer;
  private Logger logbackLogger;
  private ListAppender<ILoggingEvent> appender;

  @BeforeEach
  void setUp() {
    this.logbackLogger = (Logger) LoggerFactory.getLogger(R2UploadConsumer.class);
    this.appender = new ListAppender<>();
    this.appender.start();
    this.logbackLogger.addAppender(this.appender);

    final R2Properties properties = new R2Properties();
    properties.setEnabled(true);
    properties.setBucket("test-bucket");
    properties.setPublicUrlBase(PUBLIC_URL_BASE);
    properties.setMaxUploadAttempts(5);
    this.consumer =
        new R2UploadConsumer(
            this.mockRedisTemplate,
            new ObjectMapper(),
            this.mockClaimService,
            this.mockStorageService,
            this.mockR2StorageService,
            properties);
  }

  @AfterEach
  void tearDown() {
    this.logbackLogger.detachAppender(this.appender);
  }

  @Test
  void uploadScreenshotToR2_storesUnderScreenshotIdKeyAndSavesPublicUrl() {
    final GetObjectResponse objectResponse =
        GetObjectResponse.builder().contentType("image/jpeg").build();
    @SuppressWarnings("unchecked")
    final ResponseBytes<GetObjectResponse> responseBytes =
        (ResponseBytes<GetObjectResponse>) ResponseBytes.fromByteArray(objectResponse, FILE_BYTES);
    when(this.mockStorageService.retrieve(STORAGE_KEY)).thenReturn(responseBytes);
    when(this.mockR2StorageService.store("CAM001/CLM001", R2_FILENAME, "image/jpeg", FILE_BYTES))
        .thenReturn("CAM001/CLM001/" + R2_FILENAME);

    this.consumer.uploadScreenshotToR2(MESSAGE);

    verify(this.mockClaimService)
        .updateScreenshotPublicUrl(
            SCREENSHOT_ID, "https://cdn.example.com/CAM001/CLM001/" + R2_FILENAME);
  }

  @Test
  void processMessage_failureBelowMaxAttempts_incrementsCounterAndLogsWarn() throws Exception {
    when(this.mockStorageService.retrieve(STORAGE_KEY))
        .thenThrow(new IllegalStateException("garage unavailable"));
    when(this.mockClaimService.incrementScreenshotR2UploadAttempts(SCREENSHOT_ID)).thenReturn(2);

    this.consumer.processMessage(new ObjectMapper().writeValueAsString(MESSAGE));

    verify(this.mockClaimService).incrementScreenshotR2UploadAttempts(SCREENSHOT_ID);
    final ILoggingEvent event = this.appender.list.get(this.appender.list.size() - 1);
    assertEquals(Level.WARN, event.getLevel());
    assertTrue(event.getFormattedMessage().contains("attempt 2/5, will retry"));
  }

  @Test
  void processMessage_failureOnFinalAttempt_logsError() throws Exception {
    when(this.mockStorageService.retrieve(STORAGE_KEY))
        .thenThrow(new IllegalStateException("garage unavailable"));
    when(this.mockClaimService.incrementScreenshotR2UploadAttempts(SCREENSHOT_ID)).thenReturn(5);

    this.consumer.processMessage(new ObjectMapper().writeValueAsString(MESSAGE));

    final ILoggingEvent event = this.appender.list.get(this.appender.list.size() - 1);
    assertEquals(Level.ERROR, event.getLevel());
    assertTrue(event.getFormattedMessage().contains("final attempt 5/5, giving up"));
  }
}
