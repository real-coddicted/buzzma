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
import com.coddicted.buzzma.claim.entity.Claim;
import com.coddicted.buzzma.claim.entity.ClaimReviewStatus;
import com.coddicted.buzzma.claim.entity.ClaimStatus;
import com.coddicted.buzzma.claim.model.ClaimReviewModel;
import com.coddicted.buzzma.identity.entity.BuzzmaUser;
import com.coddicted.buzzma.identity.entity.UserRole;
import com.coddicted.buzzma.identity.persistence.UsersRepository;
import com.coddicted.buzzma.shared.enums.Platform;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
@ActiveProfiles("test")
class ClaimRepositorySearchTest {

  @Container @ServiceConnection
  static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

  @Autowired private ClaimRepository claimRepository;
  @Autowired private CampaignRepository campaignRepository;
  @Autowired private CampaignSlotRepository campaignSlotRepository;
  @Autowired private ProductRepository productRepository;
  @Autowired private DealRepository dealRepository;
  @Autowired private UsersRepository usersRepository;

  private UUID mediator1Id;
  private UUID mediator2Id;
  private UUID campaignAId;
  private UUID campaignBId;
  private UUID claim1Id;
  private UUID claim2Id;

  @BeforeEach
  void setUp() {
    final BuzzmaUser mediator1 = saveUser("Mediator One", UserRole.ROLE_MEDIATOR);
    final BuzzmaUser mediator2 = saveUser("Mediator Two", UserRole.ROLE_MEDIATOR);
    final BuzzmaUser buyer1 = saveUser("Buyer One", UserRole.ROLE_BUYER);
    final BuzzmaUser buyer2 = saveUser("Buyer Two", UserRole.ROLE_BUYER);
    this.mediator1Id = mediator1.getId();
    this.mediator2Id = mediator2.getId();

    final Campaign campaignA = saveCampaign();
    final Campaign campaignB = saveCampaign();
    this.campaignAId = campaignA.getId();
    this.campaignBId = campaignB.getId();

    final Deal dealA = saveDeal(campaignA, mediator1.getId());
    final Deal dealB = saveDeal(campaignB, mediator2.getId());

    final Claim claim1 = saveClaim(campaignA.getId(), dealA.getId(), buyer1.getId());
    final Claim claim2 = saveClaim(campaignB.getId(), dealB.getId(), buyer2.getId());
    this.claim1Id = claim1.getId();
    this.claim2Id = claim2.getId();
  }

  @Test
  void findClaimsToReviewForMediatorScopesByMediatorOnly() {
    final Page<ClaimReviewModel> result =
        this.claimRepository.findClaimsToReviewForMediator(
            this.mediator1Id, null, null, PageRequest.of(0, 20));

    assertEquals(1, result.getTotalElements());
    assertEquals(this.claim1Id, result.getContent().get(0).getClaim().getId());
  }

  @Test
  void findClaimsToReviewForMediatorAppliesCampaignIdsFilter() {
    final Page<ClaimReviewModel> matching =
        this.claimRepository.findClaimsToReviewForMediator(
            this.mediator1Id, List.of(this.campaignAId), null, PageRequest.of(0, 20));
    assertEquals(1, matching.getTotalElements());

    final Page<ClaimReviewModel> nonMatching =
        this.claimRepository.findClaimsToReviewForMediator(
            this.mediator1Id, List.of(this.campaignBId), null, PageRequest.of(0, 20));
    assertTrue(nonMatching.getContent().isEmpty());
  }

  @Test
  void findClaimsToReviewForMediatorAppliesClaimStatusFilter() {
    final Page<ClaimReviewModel> matching =
        this.claimRepository.findClaimsToReviewForMediator(
            this.mediator1Id, null, List.of(ClaimStatus.UNDER_REVIEW), PageRequest.of(0, 20));
    assertEquals(1, matching.getTotalElements());

    final Page<ClaimReviewModel> nonMatching =
        this.claimRepository.findClaimsToReviewForMediator(
            this.mediator1Id, null, List.of(ClaimStatus.APPROVED), PageRequest.of(0, 20));
    assertTrue(nonMatching.getContent().isEmpty());
  }

  @Test
  void findClaimsToReviewForCampaignsScopesByCampaignsOnly() {
    final Page<ClaimReviewModel> result =
        this.claimRepository.findClaimsToReviewForCampaigns(
            List.of(this.campaignAId, this.campaignBId), null, null, PageRequest.of(0, 20));

    assertEquals(2, result.getTotalElements());
  }

  @Test
  void findClaimsToReviewForCampaignsAppliesMediatorIdsFilter() {
    final Page<ClaimReviewModel> result =
        this.claimRepository.findClaimsToReviewForCampaigns(
            List.of(this.campaignAId, this.campaignBId),
            List.of(this.mediator1Id),
            null,
            PageRequest.of(0, 20));

    assertEquals(1, result.getTotalElements());
    assertEquals(this.claim1Id, result.getContent().get(0).getClaim().getId());
  }

  @Test
  void findClaimsToReviewForCampaignsAppliesClaimStatusFilter() {
    final Page<ClaimReviewModel> matching =
        this.claimRepository.findClaimsToReviewForCampaigns(
            List.of(this.campaignAId, this.campaignBId),
            null,
            List.of(ClaimStatus.UNDER_REVIEW),
            PageRequest.of(0, 20));
    assertEquals(2, matching.getTotalElements());

    final Page<ClaimReviewModel> nonMatching =
        this.claimRepository.findClaimsToReviewForCampaigns(
            List.of(this.campaignAId, this.campaignBId),
            null,
            List.of(ClaimStatus.APPROVED),
            PageRequest.of(0, 20));
    assertTrue(nonMatching.getContent().isEmpty());
  }

  private final AtomicInteger mobileSequence = new AtomicInteger(0);

  private BuzzmaUser saveUser(final String name, final UserRole role) {
    final String mobile = String.format("90000%05d", this.mobileSequence.incrementAndGet());
    return this.usersRepository.save(
        BuzzmaUser.builder().name(name).mobile(mobile).role(role).build());
  }

  private Campaign saveCampaign() {
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
    return campaign;
  }

  private Deal saveDeal(final Campaign campaign, final UUID mediatorId) {
    final CampaignSlot slot =
        this.campaignSlotRepository
            .findByCampaignIdInAndIsDeletedFalse(List.of(campaign.getId()))
            .get(0);
    return this.dealRepository.save(
        Deal.builder()
            .ownerId(mediatorId)
            .campaign(campaign)
            .campaignSlot(slot)
            .dealPricePaise(BigInteger.valueOf(9000))
            .isDeleted(false)
            .build());
  }

  private Claim saveClaim(final UUID campaignId, final UUID dealId, final UUID buyerId) {
    return this.claimRepository.save(
        Claim.builder()
            .campaignId(campaignId)
            .dealId(dealId)
            .ownerId(buyerId)
            .status(ClaimStatus.UNDER_REVIEW)
            .reviewStatus(ClaimReviewStatus.CLAIM_REVIEW_STATUS_PENDING)
            .platform(Platform.PLATFORM_AMAZON)
            .currentStep(CampaignStepType.REVIEW)
            .isDeleted(false)
            .build());
  }

  private static URL url(final String value) {
    try {
      return new URL(value);
    } catch (final MalformedURLException e) {
      throw new RuntimeException(e);
    }
  }
}
