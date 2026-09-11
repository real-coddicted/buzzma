package com.coddicted.buzzma.claim.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
import com.coddicted.buzzma.claim.entity.ClaimStatus;
import com.coddicted.buzzma.shared.enums.Platform;
import jakarta.persistence.EntityManager;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Verifies that {@link ClaimRepository#markApprovedClaimsReadyForAccounting} only moves {@code
 * APPROVED} claims belonging to the given agency's own campaigns into {@code READY_FOR_ACCOUNTING},
 * leaving other agencies' and other-status claims untouched, and stamps {@code updated_at}/{@code
 * updated_by} on the rows it changes.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
@ActiveProfiles("test")
class ClaimRepositoryMarkReadyForAccountingTest {

  @Container @ServiceConnection
  static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

  @Autowired private ClaimRepository claimRepository;
  @Autowired private CampaignRepository campaignRepository;
  @Autowired private CampaignSlotRepository campaignSlotRepository;
  @Autowired private ProductRepository productRepository;
  @Autowired private DealRepository dealRepository;
  @Autowired private EntityManager entityManager;

  private Campaign createCampaign(final UUID agencyOwnerId) {
    final Product product =
        this.productRepository.save(
            Product.builder()
                .name("Test product")
                .brandName("Test brand")
                .imageUrls(List.of(url("https://example.com/image.png")))
                .productLink(url("https://example.com/product"))
                .pricePaise(BigInteger.valueOf(10000))
                .build());
    return this.campaignRepository.save(
        Campaign.builder()
            .title("Test campaign")
            .ownerId(agencyOwnerId)
            .totalSlots(10)
            .product(product)
            .platform(Platform.PLATFORM_AMAZON)
            .type(CampaignType.CAMPAIGN_TYPE_REVIEW)
            .status(CampaignStatus.CAMPAIGN_STATUS_ACTIVE)
            .openToAll(false)
            .isDeleted(false)
            .build());
  }

  private Claim createClaim(final Campaign campaign, final ClaimStatus status) {
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
    return this.claimRepository.save(
        Claim.builder()
            .campaignId(campaign.getId())
            .dealId(deal.getId())
            .ownerId(UUID.randomUUID())
            .status(status)
            .platform(Platform.PLATFORM_AMAZON)
            .currentStep(CampaignStepType.REVIEW)
            .isDeleted(false)
            .build());
  }

  @Test
  void movesOnlyApprovedClaimsForTheGivenAgency() {
    final UUID agencyId = UUID.randomUUID();
    final UUID otherAgencyId = UUID.randomUUID();
    final Campaign campaign = createCampaign(agencyId);
    final Campaign otherCampaign = createCampaign(otherAgencyId);

    final Claim approved = createClaim(campaign, ClaimStatus.APPROVED);
    final Claim underReview = createClaim(campaign, ClaimStatus.UNDER_REVIEW);
    final Claim otherAgencyApproved = createClaim(otherCampaign, ClaimStatus.APPROVED);

    final int updatedCount = this.claimRepository.markApprovedClaimsReadyForAccounting(agencyId);

    assertEquals(1, updatedCount);

    // The native update bypasses the persistence context, so clear it before reloading to avoid
    // reading back the stale, pre-update entities from the first-level cache.
    this.entityManager.clear();

    final Claim reloadedApproved =
        this.claimRepository.findByIdAndIsDeletedFalse(approved.getId()).orElseThrow();
    assertEquals(ClaimStatus.READY_FOR_ACCOUNTING, reloadedApproved.getStatus());
    assertEquals(agencyId, reloadedApproved.getUpdatedBy());
    assertNotNull(reloadedApproved.getUpdatedAt());
    assertTrue(reloadedApproved.getUpdatedAt().isAfter(Instant.now().minusSeconds(60)));

    final Claim reloadedUnderReview =
        this.claimRepository.findByIdAndIsDeletedFalse(underReview.getId()).orElseThrow();
    assertEquals(ClaimStatus.UNDER_REVIEW, reloadedUnderReview.getStatus());

    final Claim reloadedOtherAgency =
        this.claimRepository.findByIdAndIsDeletedFalse(otherAgencyApproved.getId()).orElseThrow();
    assertEquals(ClaimStatus.APPROVED, reloadedOtherAgency.getStatus());
  }

  @Test
  void returnsZeroWhenNoApprovedClaimsExistForTheAgency() {
    final UUID agencyId = UUID.randomUUID();
    final Campaign campaign = createCampaign(agencyId);
    createClaim(campaign, ClaimStatus.UNDER_REVIEW);

    final int updatedCount = this.claimRepository.markApprovedClaimsReadyForAccounting(agencyId);

    assertEquals(0, updatedCount);
  }

  private static URL url(final String value) {
    try {
      return new URL(value);
    } catch (final MalformedURLException e) {
      throw new RuntimeException(e);
    }
  }
}
