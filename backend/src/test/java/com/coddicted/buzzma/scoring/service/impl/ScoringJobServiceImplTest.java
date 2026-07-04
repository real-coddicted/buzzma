package com.coddicted.buzzma.scoring.service.impl;

import static com.coddicted.buzzma.scoring.entity.ScoringJobStatus.SCORING_JOB_STATUS_COMPLETED;
import static com.coddicted.buzzma.scoring.entity.ScoringJobStatus.SCORING_JOB_STATUS_FAILED;
import static com.coddicted.buzzma.scoring.entity.ScoringJobStatus.SCORING_JOB_STATUS_PENDING;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.coddicted.buzzma.claim.service.ClaimScreenshotService;
import com.coddicted.buzzma.scoring.entity.ScoringJob;
import com.coddicted.buzzma.scoring.persistence.ScoringJobRepository;
import com.coddicted.buzzma.shared.score.ScoreApiException;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ScoringJobServiceImplTest {

  private static final UUID SCREENSHOT_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

  @Mock private ScoringJobRepository mockJobRepository;
  @Mock private ClaimScreenshotService mockClaimScreenshotService;

  private ScoringJobServiceImpl service;

  @BeforeEach
  void setUp() {
    this.service =
        new ScoringJobServiceImpl(this.mockJobRepository, this.mockClaimScreenshotService);
    when(this.mockJobRepository.save(any(ScoringJob.class))).thenAnswer(inv -> inv.getArgument(0));
  }

  private ScoringJob job(final int attemptCount) {
    return ScoringJob.builder()
        .id(UUID.randomUUID())
        .claimScreenshotId(SCREENSHOT_ID)
        .status(SCORING_JOB_STATUS_PENDING)
        .attemptCount(attemptCount)
        .build();
  }

  @Test
  void testProcessJob_success() {
    doNothing().when(this.mockClaimScreenshotService).processScoring(any(ScoringJob.class));

    final ScoringJob result = this.service.processJob(job(0));

    assertEquals(SCORING_JOB_STATUS_COMPLETED, result.getStatus());
    assertEquals(1, result.getAttemptCount());
    assertNull(result.getErrorMessage());
  }

  @Test
  void testProcessJob_scoreApiFailure_retriesUntilExhausted() {
    doThrow(new ScoreApiException("Score API call failed: timeout", null))
        .when(this.mockClaimScreenshotService)
        .processScoring(any(ScoringJob.class));

    final ScoringJob result = this.service.processJob(job(2));

    assertEquals(SCORING_JOB_STATUS_FAILED, result.getStatus());
    assertEquals(3, result.getAttemptCount());
    assertEquals("Score API call failed: timeout", result.getErrorMessage());
  }

  @Test
  void testProcessJob_scoreApiFailure_belowMaxAttemptsGoesBackToPending() {
    doThrow(new ScoreApiException("Score API call failed: timeout", null))
        .when(this.mockClaimScreenshotService)
        .processScoring(any(ScoringJob.class));

    final ScoringJob result = this.service.processJob(job(0));

    assertEquals(SCORING_JOB_STATUS_PENDING, result.getStatus());
    assertEquals(1, result.getAttemptCount());
  }

  @Test
  void testProcessJob_marksProcessingBeforeDelegating() {
    doNothing().when(this.mockClaimScreenshotService).processScoring(any(ScoringJob.class));

    this.service.processJob(job(0));

    final ArgumentCaptor<ScoringJob> captor = ArgumentCaptor.forClass(ScoringJob.class);
    verify(this.mockJobRepository, atLeastOnce()).save(captor.capture());
    assertEquals(1, captor.getAllValues().get(0).getAttemptCount());
  }
}
