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
import com.coddicted.buzzma.claim.entity.ClaimStatus;
import com.coddicted.buzzma.shared.enums.Platform;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Verifies that {@link ClaimRepository#findByIdForUpdate} takes a real row-level lock, so two
 * concurrent transactions scoring different screenshots of the same claim are serialized rather
 * than racing on the claim's aggregate score.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
@ActiveProfiles("test")
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class ClaimRepositoryLockTest {

  @Container @ServiceConnection
  static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

  @Autowired private ClaimRepository claimRepository;
  @Autowired private CampaignRepository campaignRepository;
  @Autowired private CampaignSlotRepository campaignSlotRepository;
  @Autowired private ProductRepository productRepository;
  @Autowired private DealRepository dealRepository;
  @Autowired private PlatformTransactionManager transactionManager;

  private UUID claimId;

  @BeforeEach
  void setUp() {
    // Runs as a single committed transaction so the product/campaign/slot/deal/claim entity
    // graph stays attached to one persistence context while being built (the class-level
    // NOT_SUPPORTED propagation means there's no ambient test transaction to rely on here).
    new TransactionTemplate(this.transactionManager)
        .executeWithoutResult(
            status -> {
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
            });
  }

  @Test
  void findByIdForUpdateBlocksConcurrentAccessToSameClaim() throws Exception {
    final TransactionTemplate tx = new TransactionTemplate(this.transactionManager);
    final ExecutorService executor = Executors.newFixedThreadPool(2);
    final CountDownLatch firstAcquired = new CountDownLatch(1);
    final CountDownLatch releaseFirst = new CountDownLatch(1);
    final List<String> order = new CopyOnWriteArrayList<>();

    try {
      final Future<?> first =
          executor.submit(
              () ->
                  tx.executeWithoutResult(
                      status -> {
                        this.claimRepository.findByIdForUpdate(this.claimId);
                        order.add("first-acquired");
                        firstAcquired.countDown();
                        await(releaseFirst);
                      }));

      assertTrue(firstAcquired.await(5, TimeUnit.SECONDS));

      final Future<?> second =
          executor.submit(
              () ->
                  tx.executeWithoutResult(
                      status -> {
                        this.claimRepository.findByIdForUpdate(this.claimId);
                        order.add("second-acquired");
                      }));

      // Second transaction should still be blocked on the row lock while the first holds it.
      Thread.sleep(300);
      assertEquals(List.of("first-acquired"), order);

      releaseFirst.countDown();
      first.get(5, TimeUnit.SECONDS);
      second.get(5, TimeUnit.SECONDS);

      assertEquals(List.of("first-acquired", "second-acquired"), order);
    } finally {
      executor.shutdownNow();
    }
  }

  private static void await(final CountDownLatch latch) {
    try {
      if (!latch.await(5, TimeUnit.SECONDS)) {
        throw new IllegalStateException("Timed out waiting for release signal");
      }
    } catch (final InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IllegalStateException(e);
    }
  }

  private static URL url(final String value) {
    try {
      return new URL(value);
    } catch (final MalformedURLException e) {
      throw new RuntimeException(e);
    }
  }
}
