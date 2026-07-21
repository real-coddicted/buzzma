package com.coddicted.buzzma.campaign.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.coddicted.buzzma.campaign.entity.Campaign;
import com.coddicted.buzzma.campaign.entity.CampaignSlot;
import com.coddicted.buzzma.campaign.entity.CampaignStatus;
import com.coddicted.buzzma.campaign.entity.CampaignType;
import com.coddicted.buzzma.campaign.entity.Deal;
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
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
@ActiveProfiles("test")
class DealRepositoryPublishedTest {

  @Container @ServiceConnection
  static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

  @Autowired private DealRepository dealRepository;
  @Autowired private CampaignRepository campaignRepository;
  @Autowired private CampaignSlotRepository campaignSlotRepository;

  private final UUID mediatorId = UUID.randomUUID();
  private final UUID otherMediatorId = UUID.randomUUID();
  private final UUID agencyId = UUID.randomUUID();

  private Campaign nikeCampaign;
  private Campaign adidasCampaign;

  @BeforeEach
  void setUp() {
    this.nikeCampaign = saveCampaign("Nike Campaign", "Nike");
    this.adidasCampaign = saveCampaign("Adidas Campaign", "Adidas");
    final Campaign deletedCampaign = saveCampaign("Deleted Campaign", "Reebok");
    deletedCampaign.setDeleted(true);
    this.campaignRepository.save(deletedCampaign);

    saveDeal(this.mediatorId, this.nikeCampaign, "DEAL001");
    saveDeal(this.mediatorId, this.nikeCampaign, "DEAL002");
    saveDeal(this.mediatorId, this.adidasCampaign, "DEAL003");
    saveDeal(this.otherMediatorId, this.nikeCampaign, "DEAL004");

    final Deal deletedDeal = saveDeal(this.mediatorId, this.adidasCampaign, "DEAL005");
    deletedDeal.setDeleted(true);
    this.dealRepository.save(deletedDeal);

    saveDeal(this.mediatorId, deletedCampaign, "DEAL006");
  }

  @Test
  void findsDistinctCampaignsForMediatorOnly() {
    final List<Campaign> campaigns = this.dealRepository.findCampaignsForMediator(this.mediatorId);

    assertEquals(2, campaigns.size());
    assertTrue(campaigns.stream().anyMatch(c -> c.getId().equals(this.nikeCampaign.getId())));
    assertTrue(campaigns.stream().anyMatch(c -> c.getId().equals(this.adidasCampaign.getId())));
  }

  @Test
  void excludesDeletedDealsAndCampaigns() {
    final List<Campaign> campaigns =
        this.dealRepository.findCampaignsForMediator(this.otherMediatorId);

    assertEquals(1, campaigns.size());
    assertEquals(this.nikeCampaign.getId(), campaigns.get(0).getId());
  }

  @Test
  void findsDistinctBrandNamesForMediatorOnly() {
    final List<String> brands =
        this.dealRepository.findDistinctBrandNamesForMediator(this.mediatorId);

    assertEquals(List.of("Adidas", "Nike"), brands);
  }

  private Deal saveDeal(final UUID ownerId, final Campaign campaign, final String code) {
    final CampaignSlot slot =
        this.campaignSlotRepository.save(
            CampaignSlot.builder()
                .campaignId(campaign.getId())
                .totalSlots(10)
                .slotsAvailable(9)
                .createdBy(this.agencyId)
                .isDeleted(false)
                .build());
    return this.dealRepository.save(
        Deal.builder()
            .ownerId(ownerId)
            .campaign(campaign)
            .campaignSlot(slot)
            .dealPricePaise(BigInteger.valueOf(5000))
            .code(code)
            .isDeleted(false)
            .build());
  }

  private Campaign saveCampaign(final String title, final String brand) {
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
            .title(title)
            .ownerId(this.agencyId)
            .totalSlots(10)
            .product(product)
            .platform(Platform.PLATFORM_AMAZON)
            .type(CampaignType.CAMPAIGN_TYPE_REVIEW)
            .status(CampaignStatus.CAMPAIGN_STATUS_ACTIVE)
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
