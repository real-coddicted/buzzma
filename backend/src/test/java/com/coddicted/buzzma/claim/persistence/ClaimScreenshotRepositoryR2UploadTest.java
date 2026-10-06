package com.coddicted.buzzma.claim.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
import com.coddicted.buzzma.claim.dto.PendingR2UploadView;
import com.coddicted.buzzma.claim.entity.Claim;
import com.coddicted.buzzma.claim.entity.ClaimScreenshot;
import com.coddicted.buzzma.claim.entity.ClaimStatus;
import com.coddicted.buzzma.claim.entity.ScreenshotType;
import com.coddicted.buzzma.claim.entity.ScreenshotVerificationStatus;
import com.coddicted.buzzma.shared.enums.Platform;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
@ActiveProfiles("test")
class ClaimScreenshotRepositoryR2UploadTest {

  private static final int MAX_ATTEMPTS = 5;
  private static final String CLAIM_CODE = "CLM-R2-0001";
  private static final String STORAGE_KEY = "claims/r2-pending.jpg";
  private static final String PUBLIC_URL = "https://cdn.example.com/CAM/CLM/order.jpg";

  @Container @ServiceConnection
  static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

  @Autowired private ClaimScreenshotRepository claimScreenshotRepository;
  @Autowired private ClaimRepository claimRepository;
  @Autowired private CampaignRepository campaignRepository;
  @Autowired private CampaignSlotRepository campaignSlotRepository;
  @Autowired private ProductRepository productRepository;
  @Autowired private DealRepository dealRepository;

  @Test
  void findPendingR2Uploads_returnsOnlyEligibleScreenshots() {
    final Claim claim = createClaim();
    final ClaimScreenshot eligible = saveScreenshot(claim, null, 0, false);
    saveScreenshot(claim, PUBLIC_URL, 0, false);
    saveScreenshot(claim, null, MAX_ATTEMPTS, false);
    saveScreenshot(claim, null, 0, true);

    final List<PendingR2UploadView> result =
        this.claimScreenshotRepository.findPendingR2Uploads(
            Instant.now().plus(Duration.ofMinutes(1)), MAX_ATTEMPTS, PageRequest.of(0, 10));

    assertEquals(
        List.of(
            new PendingR2UploadView(
                eligible.getId(),
                claim.getId(),
                CLAIM_CODE,
                claim.getCampaignId(),
                STORAGE_KEY,
                ScreenshotType.SCREENSHOT_TYPE_ORDER)),
        result);
  }

  @Test
  void findPendingR2Uploads_excludesScreenshotsNewerThanCutoff() {
    final Claim claim = createClaim();
    saveScreenshot(claim, null, 0, false);

    final List<PendingR2UploadView> result =
        this.claimScreenshotRepository.findPendingR2Uploads(
            Instant.now().minus(Duration.ofMinutes(30)), MAX_ATTEMPTS, PageRequest.of(0, 10));

    assertTrue(result.isEmpty());
  }

  @Test
  void incrementR2UploadAttempts_incrementsStoredCount() {
    final ClaimScreenshot screenshot = saveScreenshot(createClaim(), null, 2, false);

    this.claimScreenshotRepository.incrementR2UploadAttempts(screenshot.getId());

    assertEquals(
        Optional.of(3), this.claimScreenshotRepository.findR2UploadAttempts(screenshot.getId()));
  }

  private ClaimScreenshot saveScreenshot(
      final Claim claim, final String publicUrl, final int attempts, final boolean deleted) {
    return this.claimScreenshotRepository.saveAndFlush(
        ClaimScreenshot.builder()
            .claimId(claim.getId())
            .storageKey(STORAGE_KEY)
            .type(ScreenshotType.SCREENSHOT_TYPE_ORDER)
            .verificationStatus(ScreenshotVerificationStatus.SCREENSHOT_VERIFICATION_STATUS_PENDING)
            .publicUrl(publicUrl)
            .r2UploadAttempts(attempts)
            .isDeleted(deleted)
            .build());
  }

  private Claim createClaim() {
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
    return this.claimRepository.save(
        Claim.builder()
            .code(CLAIM_CODE)
            .campaignId(campaign.getId())
            .dealId(deal.getId())
            .ownerId(UUID.randomUUID())
            .status(ClaimStatus.ORDERED)
            .platform(Platform.PLATFORM_AMAZON)
            .currentStep(CampaignStepType.ORDER)
            .isDeleted(false)
            .build());
  }

  private static URL url(final String value) {
    try {
      return new URL(value);
    } catch (final MalformedURLException e) {
      throw new IllegalStateException(e);
    }
  }
}
