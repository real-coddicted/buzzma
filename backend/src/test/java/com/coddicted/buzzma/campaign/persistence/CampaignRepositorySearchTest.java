package com.coddicted.buzzma.campaign.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.coddicted.buzzma.campaign.entity.Campaign;
import com.coddicted.buzzma.campaign.entity.CampaignStatus;
import com.coddicted.buzzma.campaign.entity.CampaignType;
import com.coddicted.buzzma.campaign.entity.Product;
import com.coddicted.buzzma.shared.enums.Platform;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.UUID;
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
class CampaignRepositorySearchTest {

  @Container @ServiceConnection
  static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

  @Autowired private CampaignRepository campaignRepository;

  private final UUID ownerId = UUID.randomUUID();
  private final UUID otherOwnerId = UUID.randomUUID();

  @BeforeEach
  void setUp() {
    save(
        this.ownerId,
        "Nike",
        Platform.PLATFORM_AMAZON,
        CampaignType.CAMPAIGN_TYPE_REVIEW,
        CampaignStatus.CAMPAIGN_STATUS_ACTIVE,
        20240101);
    save(
        this.ownerId,
        "Adidas",
        Platform.PLATFORM_FLIPKART,
        CampaignType.CAMPAIGN_TYPE_RATING,
        CampaignStatus.CAMPAIGN_STATUS_PAUSED,
        20240601);
    save(
        this.ownerId,
        "Nike",
        Platform.PLATFORM_AMAZON,
        CampaignType.CAMPAIGN_TYPE_REVIEW,
        CampaignStatus.CAMPAIGN_STATUS_DRAFT,
        null);
    save(
        this.otherOwnerId,
        "Nike",
        Platform.PLATFORM_AMAZON,
        CampaignType.CAMPAIGN_TYPE_REVIEW,
        CampaignStatus.CAMPAIGN_STATUS_ACTIVE,
        20240301);
  }

  @Test
  void filtersByOwnerOnly() {
    final Page<Campaign> result =
        this.campaignRepository.search(
            this.ownerId, null, null, null, null, null, null, PageRequest.of(0, 20));

    assertEquals(3, result.getTotalElements());
  }

  @Test
  void filtersByBrandCaseInsensitive() {
    final Page<Campaign> result =
        this.campaignRepository.search(
            this.ownerId, List.of("nike"), null, null, null, null, null, PageRequest.of(0, 20));

    assertEquals(2, result.getTotalElements());
  }

  @Test
  void filtersByPlatformTypeAndStatus() {
    final Page<Campaign> result =
        this.campaignRepository.search(
            this.ownerId,
            null,
            List.of(Platform.PLATFORM_FLIPKART),
            List.of(CampaignType.CAMPAIGN_TYPE_RATING),
            List.of(CampaignStatus.CAMPAIGN_STATUS_PAUSED),
            null,
            null,
            PageRequest.of(0, 20));

    assertEquals(1, result.getTotalElements());
    assertEquals("Adidas", result.getContent().get(0).getProduct().getBrandName());
  }

  @Test
  void filtersByStartDateRange() {
    final Page<Campaign> result =
        this.campaignRepository.search(
            this.ownerId, null, null, null, null, 20240101, 20240101, PageRequest.of(0, 20));

    assertEquals(1, result.getTotalElements());
  }

  @Test
  void filtersByDateRangeWhenOnlyEndDateFallsWithinWindow() {
    final Campaign campaignEndingInWindow =
        save(
            this.ownerId,
            "Reebok",
            Platform.PLATFORM_AMAZON,
            CampaignType.CAMPAIGN_TYPE_REVIEW,
            CampaignStatus.CAMPAIGN_STATUS_ACTIVE,
            20230101,
            20240115);

    final Page<Campaign> result =
        this.campaignRepository.search(
            this.ownerId, null, null, null, null, 20240110, 20240120, PageRequest.of(0, 20));

    assertEquals(1, result.getTotalElements());
    assertEquals(campaignEndingInWindow.getId(), result.getContent().get(0).getId());
  }

  @Test
  void ordersByStartDateDescendingWithNullsLast() {
    final Page<Campaign> result =
        this.campaignRepository.search(
            this.ownerId, null, null, null, null, null, null, PageRequest.of(0, 20));

    final List<Integer> startDates =
        result.getContent().stream().map(Campaign::getStartDate).toList();
    assertEquals(java.util.Arrays.asList(20240601, 20240101, null), startDates);
  }

  private void save(
      final UUID ownerId,
      final String brand,
      final Platform platform,
      final CampaignType type,
      final CampaignStatus status,
      final Integer startDate) {
    save(ownerId, brand, platform, type, status, startDate, null);
  }

  private Campaign save(
      final UUID ownerId,
      final String brand,
      final Platform platform,
      final CampaignType type,
      final CampaignStatus status,
      final Integer startDate,
      final Integer endDate) {
    final Product product =
        Product.builder()
            .name("Test product")
            .brandName(brand)
            .imageUrl(url("https://example.com/image.png"))
            .productLink(url("https://example.com/product"))
            .pricePaise(BigInteger.valueOf(10000))
            .build();
    final Campaign campaign =
        Campaign.builder()
            .title("Test campaign")
            .ownerId(ownerId)
            .totalSlots(10)
            .product(product)
            .platform(platform)
            .type(type)
            .status(status)
            .startDate(startDate)
            .endDate(endDate)
            .openToAll(false)
            .isDeleted(false)
            .build();
    return this.campaignRepository.save(campaign);
  }

  private static URL url(final String value) {
    try {
      return new URI(value).toURL();
    } catch (final MalformedURLException | URISyntaxException e) {
      throw new RuntimeException(e);
    }
  }
}
