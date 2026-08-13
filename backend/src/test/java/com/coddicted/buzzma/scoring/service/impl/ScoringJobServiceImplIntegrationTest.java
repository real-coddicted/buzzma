package com.coddicted.buzzma.scoring.service.impl;

import static com.coddicted.buzzma.scoring.entity.ScoringJobStatus.SCORING_JOB_STATUS_COMPLETED;
import static com.coddicted.buzzma.scoring.entity.ScoringJobStatus.SCORING_JOB_STATUS_FAILED;
import static com.coddicted.buzzma.scoring.entity.ScoringJobStatus.SCORING_JOB_STATUS_PENDING;
import static com.coddicted.buzzma.scoring.entity.ScoringJobStatus.SCORING_JOB_STATUS_PROCESSING;
import static org.assertj.core.api.Assertions.assertThat;

import com.coddicted.buzzma.campaign.entity.Campaign;
import com.coddicted.buzzma.campaign.entity.CampaignSlot;
import com.coddicted.buzzma.campaign.entity.CampaignStatus;
import com.coddicted.buzzma.campaign.entity.CampaignStepType;
import com.coddicted.buzzma.campaign.entity.CampaignType;
import com.coddicted.buzzma.campaign.entity.Deal;
import com.coddicted.buzzma.campaign.entity.Product;
import com.coddicted.buzzma.campaign.persistence.CampaignRepository;
import com.coddicted.buzzma.campaign.persistence.CampaignSlotRepository;
import com.coddicted.buzzma.campaign.persistence.DealRepository;
import com.coddicted.buzzma.campaign.persistence.ProductRepository;
import com.coddicted.buzzma.claim.entity.Claim;
import com.coddicted.buzzma.claim.entity.ClaimScreenshot;
import com.coddicted.buzzma.claim.entity.ClaimStatus;
import com.coddicted.buzzma.claim.entity.ScreenshotType;
import com.coddicted.buzzma.claim.entity.ScreenshotVerificationStatus;
import com.coddicted.buzzma.claim.persistence.ClaimRepository;
import com.coddicted.buzzma.claim.persistence.ClaimScreenshotRepository;
import com.coddicted.buzzma.claim.service.ClaimScreenshotService;
import com.coddicted.buzzma.scoring.entity.ScoringJob;
import com.coddicted.buzzma.scoring.persistence.ScoringJobRepository;
import com.coddicted.buzzma.shared.enums.Platform;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Verifies that {@link ScoringJobServiceImpl#claimBatchForProcessing} atomically claims a batch of
 * PENDING jobs (flipping them to PROCESSING and bumping attempt_count in one statement), so
 * concurrent scheduler instances never pick up the same job twice.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
@ActiveProfiles("test")
@Import(ScoringJobServiceImpl.class)
class ScoringJobServiceImplIntegrationTest {

  @Container @ServiceConnection
  static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

  @Autowired private ScoringJobServiceImpl service;
  @Autowired private ScoringJobRepository jobRepository;
  @Autowired private ClaimScreenshotRepository screenshotRepository;
  @Autowired private ClaimRepository claimRepository;
  @Autowired private CampaignRepository campaignRepository;
  @Autowired private CampaignSlotRepository campaignSlotRepository;
  @Autowired private ProductRepository productRepository;
  @Autowired private DealRepository dealRepository;
  @Autowired private TestEntityManager testEntityManager;

  @MockBean private ClaimScreenshotService claimScreenshotService;

  private UUID claimId;

  @BeforeEach
  void setUp() {
    final Product product =
        this.productRepository.save(
            Product.builder()
                .name("Test product")
                .brandName("Test brand")
                .imageUrl(url("https://example.com/image.png"))
                .productLink(url("https://example.com/product"))
                .pricePaise(BigInteger.valueOf(10000))
                .build());
    final Campaign campaign =
        this.campaignRepository.save(
            Campaign.builder()
                .title("Test campaign")
                .ownerId(UUID.randomUUID())
                .totalSlots(10)
                .product(product)
                .platform(Platform.PLATFORM_AMAZON)
                .type(CampaignType.CAMPAIGN_TYPE_REVIEW)
                .status(CampaignStatus.CAMPAIGN_STATUS_ACTIVE)
                .openToAll(false)
                .isDeleted(false)
                .build());
    this.campaignSlotRepository.save(
        CampaignSlot.builder()
            .campaignId(campaign.getId())
            .totalSlots(10)
            .slotsAvailable(9)
            .createdBy(campaign.getOwnerId())
            .isDeleted(false)
            .build());
    final CampaignSlot slot =
        this.campaignSlotRepository
            .findByCampaignIdInAndIsDeletedFalse(List.of(campaign.getId()))
            .get(0);
    final Deal deal =
        this.dealRepository.save(
            Deal.builder()
                .ownerId(UUID.randomUUID())
                .campaign(campaign)
                .campaignSlot(slot)
                .dealPricePaise(BigInteger.valueOf(9000))
                .isDeleted(false)
                .build());
    final Claim claim =
        this.claimRepository.save(
            Claim.builder()
                .campaignId(campaign.getId())
                .dealId(deal.getId())
                .ownerId(UUID.randomUUID())
                .status(ClaimStatus.UNDER_REVIEW)
                .platform(Platform.PLATFORM_AMAZON)
                .currentStep(CampaignStepType.REVIEW)
                .isDeleted(false)
                .build());
    this.claimId = claim.getId();
  }

  private ScoringJob pendingJob(final int attemptCount) {
    final ClaimScreenshot screenshot =
        this.screenshotRepository.save(
            ClaimScreenshot.builder()
                .claimId(this.claimId)
                .storageKey("claims/screenshot.jpg")
                .type(ScreenshotType.SCREENSHOT_TYPE_ORDER)
                .verificationStatus(
                    ScreenshotVerificationStatus.SCREENSHOT_VERIFICATION_STATUS_PENDING)
                .build());
    return this.jobRepository.save(
        ScoringJob.builder()
            .claimScreenshotId(screenshot.getId())
            .status(SCORING_JOB_STATUS_PENDING)
            .attemptCount(attemptCount)
            .build());
  }

  @Test
  void claimBatchForProcessingFlipsPendingJobsToProcessingAndIncrementsAttempts() {
    final ScoringJob job1 = pendingJob(0);
    final ScoringJob job2 = pendingJob(1);

    final List<UUID> claimed = this.service.claimBatchForProcessing(5, 3);

    assertThat(claimed).containsExactlyInAnyOrder(job1.getId(), job2.getId());

    // claimBatchForProcessing updates rows via a native query, which bypasses the persistence
    // context — clear it so the reload below hits the database instead of the stale first-level
    // cache entry created by pendingJob()'s save().
    this.testEntityManager.clear();
    final ScoringJob reloaded1 = this.jobRepository.findById(job1.getId()).orElseThrow();
    final ScoringJob reloaded2 = this.jobRepository.findById(job2.getId()).orElseThrow();
    assertThat(reloaded1.getStatus()).isEqualTo(SCORING_JOB_STATUS_PROCESSING);
    assertThat(reloaded1.getAttemptCount()).isEqualTo(1);
    assertThat(reloaded2.getStatus()).isEqualTo(SCORING_JOB_STATUS_PROCESSING);
    assertThat(reloaded2.getAttemptCount()).isEqualTo(2);
  }

  @Test
  void claimBatchForProcessingExcludesJobsAtOrAboveMaxAttempts() {
    final ScoringJob exhausted = pendingJob(3);

    final List<UUID> claimed = this.service.claimBatchForProcessing(5, 3);

    assertThat(claimed).doesNotContain(exhausted.getId());
    this.testEntityManager.clear();
    final ScoringJob reloaded = this.jobRepository.findById(exhausted.getId()).orElseThrow();
    assertThat(reloaded.getStatus()).isEqualTo(SCORING_JOB_STATUS_PENDING);
  }

  @Test
  void claimBatchForProcessingExcludesNonPendingJobs() {
    final ScoringJob processing = pendingJob(0);
    this.jobRepository.save(processing.toBuilder().status(SCORING_JOB_STATUS_PROCESSING).build());
    final ScoringJob completed = pendingJob(1);
    this.jobRepository.save(completed.toBuilder().status(SCORING_JOB_STATUS_COMPLETED).build());
    final ScoringJob failed = pendingJob(3);
    this.jobRepository.save(failed.toBuilder().status(SCORING_JOB_STATUS_FAILED).build());

    final List<UUID> claimed = this.service.claimBatchForProcessing(5, 3);

    assertThat(claimed).isEmpty();
  }

  @Test
  void claimBatchForProcessingRespectsBatchSize() {
    pendingJob(0);
    pendingJob(0);
    pendingJob(0);

    final List<UUID> claimed = this.service.claimBatchForProcessing(2, 3);

    assertThat(claimed).hasSize(2);
  }

  private static URL url(final String value) {
    try {
      return new URL(value);
    } catch (final MalformedURLException e) {
      throw new RuntimeException(e);
    }
  }
}
