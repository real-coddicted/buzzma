package com.coddicted.buzzma.campaign.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.coddicted.buzzma.campaign.entity.Campaign;
import com.coddicted.buzzma.campaign.entity.CampaignSlot;
import com.coddicted.buzzma.campaign.entity.CampaignStatus;
import com.coddicted.buzzma.campaign.entity.CampaignType;
import com.coddicted.buzzma.campaign.entity.Product;
import com.coddicted.buzzma.shared.enums.Platform;
import com.coddicted.buzzma.shared.util.DateTimeUtils;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
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

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
@ActiveProfiles("test")
class CampaignRepositoryShareableCampaignsTest {

  @Container @ServiceConnection
  static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

  @Autowired private CampaignRepository campaignRepository;
  @Autowired private CampaignSlotRepository campaignSlotRepository;

  private final UUID ownerId = UUID.randomUUID();

  @Test
  void collapsesCampaignWithMultipleSlotRowsIntoOneRowWithSummedSlots() {
    final Campaign campaign = saveCampaign();
    saveSlot(campaign.getId(), 10, 4);
    saveSlot(campaign.getId(), 5, 5);

    final List<ShareableCampaignView> result =
        this.campaignRepository.findShareableCampaigns(
            this.ownerId, DateTimeUtils.getAsianTodayDate());

    assertEquals(1, result.size());
    assertEquals(campaign.getId(), result.get(0).getCampaignId());
    assertEquals(15, result.get(0).getTotalSlots());
    assertEquals(9, result.get(0).getSlotsAvailable());
  }

  @Test
  void returnsOriginalSlotCountsForSingleSlotRowCampaign() {
    final Campaign campaign = saveCampaign();
    saveSlot(campaign.getId(), 10, 7);

    final List<ShareableCampaignView> result =
        this.campaignRepository.findShareableCampaigns(
            this.ownerId, DateTimeUtils.getAsianTodayDate());

    assertEquals(1, result.size());
    assertEquals(campaign.getId(), result.get(0).getCampaignId());
    assertEquals(10, result.get(0).getTotalSlots());
    assertEquals(7, result.get(0).getSlotsAvailable());
  }

  private Campaign saveCampaign() {
    final Product product =
        Product.builder()
            .name("Test product")
            .brandName("Nike")
            .imageUrl(url("https://example.com/image.png"))
            .productLink(url("https://example.com/product"))
            .pricePaise(BigInteger.valueOf(10000))
            .build();
    final Campaign campaign =
        Campaign.builder()
            .title("Test campaign")
            .ownerId(this.ownerId)
            .totalSlots(15)
            .product(product)
            .platform(Platform.PLATFORM_AMAZON)
            .type(CampaignType.CAMPAIGN_TYPE_REVIEW)
            .status(CampaignStatus.CAMPAIGN_STATUS_ACTIVE)
            .startDate(20240101)
            .endDate(20990101)
            .openToAll(false)
            .isDeleted(false)
            .build();
    return this.campaignRepository.save(campaign);
  }

  private void saveSlot(final UUID campaignId, final int totalSlots, final int slotsAvailable) {
    this.campaignSlotRepository.save(
        CampaignSlot.builder()
            .campaignId(campaignId)
            .totalSlots(totalSlots)
            .slotsAvailable(slotsAvailable)
            .createdBy(this.ownerId)
            .isDeleted(false)
            .build());
  }

  private static URL url(final String value) {
    try {
      return new URI(value).toURL();
    } catch (final MalformedURLException | URISyntaxException e) {
      throw new RuntimeException(e);
    }
  }
}
