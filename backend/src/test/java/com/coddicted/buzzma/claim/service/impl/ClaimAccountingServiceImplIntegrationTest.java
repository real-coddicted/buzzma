package com.coddicted.buzzma.claim.service.impl;

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
import com.coddicted.buzzma.campaign.service.CampaignAssignmentService;
import com.coddicted.buzzma.campaign.service.CampaignService;
import com.coddicted.buzzma.campaign.service.CommissionService;
import com.coddicted.buzzma.campaign.service.DealService;
import com.coddicted.buzzma.claim.entity.Claim;
import com.coddicted.buzzma.claim.entity.ClaimAccountingStatus;
import com.coddicted.buzzma.claim.entity.ClaimStatus;
import com.coddicted.buzzma.claim.persistence.ClaimRepository;
import com.coddicted.buzzma.claim.service.ClaimService;
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
 * Verifies that the accounting pipeline keys off the dedicated {@code READY_FOR_ACCOUNTING} claim
 * status (issue #789): {@link ClaimAccountingServiceImpl#claimBatchForProcessing} only picks up
 * claims in that status, and {@link ClaimRepository#markAccountingCompleted} moves a claim to
 * {@code REWARD_PENDING} once accounting finishes.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
@ActiveProfiles("test")
@Import(ClaimAccountingServiceImpl.class)
class ClaimAccountingServiceImplIntegrationTest {

  @Container @ServiceConnection
  static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

  @Autowired private ClaimAccountingServiceImpl service;
  @Autowired private ClaimRepository claimRepository;
  @Autowired private CampaignRepository campaignRepository;
  @Autowired private CampaignSlotRepository campaignSlotRepository;
  @Autowired private ProductRepository productRepository;
  @Autowired private DealRepository dealRepository;
  @Autowired private TestEntityManager testEntityManager;

  // ClaimAccountingServiceImpl's non-repository collaborators — never exercised by the two methods
  // under test, but required to construct the bean.
  @MockBean private ClaimService claimService;
  @MockBean private DealService dealService;
  @MockBean private CampaignService campaignService;
  @MockBean private CampaignAssignmentService campaignAssignmentService;
  @MockBean private CommissionService commissionService;

  private UUID campaignId;
  private UUID dealId;

  @BeforeEach
  void setUp() {
    final Product product =
        this.productRepository.save(
            Product.builder()
                .name("Test product")
                .brandName("Test brand")
                .imageUrls(List.of(url("https://example.com/image.png")))
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
    this.campaignId = campaign.getId();
    this.dealId = deal.getId();
  }

  private Claim persistClaim(
      final ClaimStatus status, final ClaimAccountingStatus accountingStatus) {
    return this.claimRepository.save(
        Claim.builder()
            .campaignId(this.campaignId)
            .dealId(this.dealId)
            .ownerId(UUID.randomUUID())
            .status(status)
            .accountingStatus(accountingStatus)
            .platform(Platform.PLATFORM_AMAZON)
            .currentStep(CampaignStepType.REVIEW)
            .isDeleted(false)
            .build());
  }

  @Test
  void claimBatchForProcessingPicksOnlyReadyForAccountingClaims() {
    final Claim ready =
        persistClaim(ClaimStatus.READY_FOR_ACCOUNTING, ClaimAccountingStatus.PENDING);
    final Claim readyRetry =
        persistClaim(ClaimStatus.READY_FOR_ACCOUNTING, ClaimAccountingStatus.FAILED);
    final Claim stillApproved = persistClaim(ClaimStatus.APPROVED, ClaimAccountingStatus.PENDING);

    final List<UUID> claimed = this.service.claimBatchForProcessing(10, 3);

    assertThat(claimed).containsExactlyInAnyOrder(ready.getId(), readyRetry.getId());
    assertThat(claimed).doesNotContain(stillApproved.getId());

    // The picker flips rows via a native query that bypasses the persistence context.
    this.testEntityManager.clear();
    assertThat(this.claimRepository.findById(ready.getId()).orElseThrow().getAccountingStatus())
        .isEqualTo(ClaimAccountingStatus.IN_PROGRESS);
    assertThat(this.claimRepository.findById(stillApproved.getId()).orElseThrow().getStatus())
        .isEqualTo(ClaimStatus.APPROVED);
  }

  @Test
  void markAccountingCompletedMovesClaimToRewardPending() {
    final Claim claim =
        persistClaim(ClaimStatus.READY_FOR_ACCOUNTING, ClaimAccountingStatus.IN_PROGRESS);

    this.claimRepository.markAccountingCompleted(claim.getId());

    this.testEntityManager.clear();
    final Claim reloaded = this.claimRepository.findById(claim.getId()).orElseThrow();
    assertThat(reloaded.getAccountingStatus()).isEqualTo(ClaimAccountingStatus.COMPLETED);
    assertThat(reloaded.getStatus()).isEqualTo(ClaimStatus.REWARD_PENDING);
  }

  private static URL url(final String value) {
    try {
      return new URL(value);
    } catch (final MalformedURLException e) {
      throw new RuntimeException(e);
    }
  }
}
