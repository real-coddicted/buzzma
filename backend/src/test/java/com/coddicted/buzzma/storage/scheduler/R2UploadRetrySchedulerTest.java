package com.coddicted.buzzma.storage.scheduler;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.coddicted.buzzma.claim.service.ClaimService;
import com.coddicted.buzzma.storage.config.R2Properties;
import com.coddicted.buzzma.storage.event.R2UploadMessage;
import com.coddicted.buzzma.storage.event.R2UploadPublisher;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class R2UploadRetrySchedulerTest {

  private static final Instant NOW = Instant.parse("2026-10-01T10:30:00Z");
  private static final Instant CREATED_BEFORE = Instant.parse("2026-10-01T10:00:00Z");
  private static final int MAX_ATTEMPTS = 5;
  private static final int BATCH_SIZE = 500;

  private static final R2UploadMessage MESSAGE_1 =
      new R2UploadMessage(
          UUID.fromString("11111111-1111-1111-1111-111111111111"),
          UUID.fromString("22222222-2222-2222-2222-222222222222"),
          "CAM001",
          "CLM001",
          "claims/one.jpg",
          "SCREENSHOT_TYPE_ORDER");
  private static final R2UploadMessage MESSAGE_2 =
      new R2UploadMessage(
          UUID.fromString("33333333-3333-3333-3333-333333333333"),
          UUID.fromString("22222222-2222-2222-2222-222222222222"),
          "CAM001",
          "CLM001",
          "claims/two.jpg",
          "SCREENSHOT_TYPE_RATING");

  @Mock private ClaimService mockClaimService;
  @Mock private R2UploadPublisher mockPublisher;

  private R2UploadRetryScheduler scheduler;

  @BeforeEach
  void setUp() {
    final R2Properties properties = new R2Properties();
    properties.setMaxUploadAttempts(MAX_ATTEMPTS);
    this.scheduler =
        new R2UploadRetryScheduler(
            this.mockClaimService, this.mockPublisher, properties, 30, BATCH_SIZE);
  }

  @Test
  void requeuePendingUploads_enqueuesEveryPendingScreenshot() {
    when(this.mockClaimService.listPendingR2Uploads(CREATED_BEFORE, MAX_ATTEMPTS, BATCH_SIZE))
        .thenReturn(List.of(MESSAGE_1, MESSAGE_2));

    this.scheduler.requeuePendingUploads(NOW);

    verify(this.mockPublisher).enqueue(MESSAGE_1);
    verify(this.mockPublisher).enqueue(MESSAGE_2);
  }

  @Test
  void requeuePendingUploads_doesNothingWhenNothingPending() {
    when(this.mockClaimService.listPendingR2Uploads(CREATED_BEFORE, MAX_ATTEMPTS, BATCH_SIZE))
        .thenReturn(List.of());

    this.scheduler.requeuePendingUploads(NOW);

    verifyNoInteractions(this.mockPublisher);
  }
}
