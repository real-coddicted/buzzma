package com.coddicted.buzzma.campaign.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.coddicted.buzzma.campaign.entity.Campaign;
import com.coddicted.buzzma.campaign.entity.CampaignShare;
import com.coddicted.buzzma.campaign.entity.CampaignStatus;
import com.coddicted.buzzma.campaign.entity.CampaignType;
import com.coddicted.buzzma.campaign.entity.Product;
import com.coddicted.buzzma.identity.entity.BuzzmaUser;
import com.coddicted.buzzma.identity.entity.UserRole;
import com.coddicted.buzzma.identity.entity.UserStatus;
import com.coddicted.buzzma.identity.persistence.UsersRepository;
import com.coddicted.buzzma.shared.enums.Platform;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.Instant;
import java.util.UUID;
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
class CampaignRepositorySharedByOwnerTest {

  @Container @ServiceConnection
  static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

  @Autowired private CampaignRepository campaignRepository;
  @Autowired private CampaignShareRepository campaignShareRepository;
  @Autowired private UsersRepository usersRepository;

  private final UUID ownerId = UUID.randomUUID();

  @Test
  void returnsCampaignSharedByOwnerWithBrandNameAndDate() {
    final BuzzmaUser brand = saveBrand("Acme Brand", "AC-001");
    final Campaign campaign = saveCampaign();
    saveShare(campaign.getId(), this.ownerId, brand.getId());

    final Page<SharedCampaignSummaryView> result =
        this.campaignRepository.findCampaignsSharedByOwner(this.ownerId, PageRequest.of(0, 20));

    assertEquals(1, result.getContent().size());
    assertEquals(campaign.getTitle(), result.getContent().get(0).getCampaignName());
    assertEquals(campaign.getCode(), result.getContent().get(0).getCampaignCode());
    assertEquals(brand.getId(), result.getContent().get(0).getSharedWithUserId());
    assertEquals("Acme Brand", result.getContent().get(0).getSharedWithUserName());
    assertEquals("AC-001", result.getContent().get(0).getSharedWithUserCode());
    assertTrue(result.getContent().get(0).getSharedAt() != null);
  }

  @Test
  void excludesCampaignsSharedByOtherOwners() {
    final BuzzmaUser brand = saveBrand("Other Brand", "OB-001");
    final Campaign campaign = saveCampaign();
    saveShare(campaign.getId(), UUID.randomUUID(), brand.getId());

    final Page<SharedCampaignSummaryView> result =
        this.campaignRepository.findCampaignsSharedByOwner(this.ownerId, PageRequest.of(0, 20));

    assertEquals(0, result.getContent().size());
  }

  private BuzzmaUser saveBrand(final String name, final String code) {
    return this.usersRepository.save(
        BuzzmaUser.builder()
            .name(name)
            .code(code)
            .mobile("9876543210")
            .role(UserRole.ROLE_BRAND)
            .status(UserStatus.USER_STATUS_ACTIVE)
            .isDeleted(false)
            .build());
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
            .code("CAMP-" + UUID.randomUUID().toString().substring(0, 8))
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

  private void saveShare(final UUID campaignId, final UUID fromUserId, final UUID toUserId) {
    this.campaignShareRepository.save(
        CampaignShare.builder()
            .campaignId(campaignId)
            .fromUserId(fromUserId)
            .toUserId(toUserId)
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .createdBy(fromUserId)
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
