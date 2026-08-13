package com.coddicted.buzzma.campaign.persistence;

import static com.coddicted.buzzma.campaign.persistence.Fixtures.AGENCY_USER;
import static com.coddicted.buzzma.campaign.persistence.Fixtures.BRAND_ID;
import static com.coddicted.buzzma.campaign.persistence.Fixtures.OTHER_BRAND_ID;
import static com.coddicted.buzzma.campaign.persistence.Fixtures.SHARED_CAMPAIGN_1;
import static com.coddicted.buzzma.campaign.persistence.Fixtures.SHARED_CAMPAIGN_2;
import static com.coddicted.buzzma.campaign.persistence.Fixtures.SHARED_CAMPAIGN_3;
import static com.coddicted.buzzma.campaign.persistence.Fixtures.SHARED_CAMPAIGN_4;
import static com.coddicted.buzzma.campaign.persistence.Fixtures.SHARED_CAMPAIGN_5;
import static com.coddicted.buzzma.campaign.persistence.Fixtures.SHARED_CAMPAIGN_6;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.coddicted.buzzma.campaign.entity.Campaign;
import com.coddicted.buzzma.campaign.entity.CampaignShare;
import com.coddicted.buzzma.campaign.entity.CampaignStatus;
import com.coddicted.buzzma.identity.entity.BuzzmaUser;
import com.coddicted.buzzma.identity.persistence.UsersRepository;
import java.time.Instant;
import java.util.List;
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
class CampaignRepositorySharedCampaignsTest {

  @Container @ServiceConnection
  static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

  @Autowired private CampaignRepository campaignRepository;
  @Autowired private CampaignShareRepository campaignShareRepository;
  @Autowired private UsersRepository usersRepository;

  @Test
  void returnsSecondPageWithCorrectTotals() {
    final BuzzmaUser agency = this.usersRepository.save(AGENCY_USER.toBuilder().build());
    final List<Campaign> campaigns =
        List.of(
            saveCampaignForOwner(SHARED_CAMPAIGN_1, agency.getId()),
            saveCampaignForOwner(SHARED_CAMPAIGN_2, agency.getId()),
            saveCampaignForOwner(SHARED_CAMPAIGN_3, agency.getId()));
    campaigns.forEach(c -> saveShare(c.getId(), agency.getId(), BRAND_ID));

    final Page<Campaign> firstPage = findShared(BRAND_ID, PageRequest.of(0, 2));
    final Page<Campaign> secondPage = findShared(BRAND_ID, PageRequest.of(1, 2));

    assertEquals(3, firstPage.getTotalElements());
    assertEquals(2, firstPage.getTotalPages());
    assertEquals(2, firstPage.getContent().size());
    assertEquals(1, secondPage.getContent().size());
  }

  @Test
  void excludesCampaignsSharedToOtherBrands() {
    final BuzzmaUser agency = this.usersRepository.save(AGENCY_USER.toBuilder().build());
    final Campaign campaign = saveCampaignForOwner(SHARED_CAMPAIGN_4, agency.getId());
    saveShare(campaign.getId(), agency.getId(), OTHER_BRAND_ID);

    final Page<Campaign> result = findShared(BRAND_ID, PageRequest.of(0, 20));

    assertEquals(0, result.getTotalElements());
  }

  @Test
  void filtersByBrandNameCaseInsensitively() {
    final BuzzmaUser agency = this.usersRepository.save(AGENCY_USER.toBuilder().build());
    final Campaign campaign = saveCampaignForOwner(SHARED_CAMPAIGN_5, agency.getId());
    saveShare(campaign.getId(), agency.getId(), BRAND_ID);

    final Page<Campaign> matching =
        this.campaignRepository.findSharedCampaigns(
            BRAND_ID, List.of("test brand"), null, null, null, null, null, PageRequest.of(0, 20));
    final Page<Campaign> nonMatching =
        this.campaignRepository.findSharedCampaigns(
            BRAND_ID,
            List.of("some other brand"),
            null,
            null,
            null,
            null,
            null,
            PageRequest.of(0, 20));

    assertEquals(1, matching.getTotalElements());
    assertEquals(0, nonMatching.getTotalElements());
  }

  @Test
  void filtersByStatus() {
    final BuzzmaUser agency = this.usersRepository.save(AGENCY_USER.toBuilder().build());
    final Campaign campaign = saveCampaignForOwner(SHARED_CAMPAIGN_6, agency.getId());
    saveShare(campaign.getId(), agency.getId(), BRAND_ID);

    final Page<Campaign> matching =
        this.campaignRepository.findSharedCampaigns(
            BRAND_ID,
            null,
            null,
            null,
            List.of(CampaignStatus.CAMPAIGN_STATUS_ACTIVE),
            null,
            null,
            PageRequest.of(0, 20));
    final Page<Campaign> nonMatching =
        this.campaignRepository.findSharedCampaigns(
            BRAND_ID,
            null,
            null,
            null,
            List.of(CampaignStatus.CAMPAIGN_STATUS_CLOSED),
            null,
            null,
            PageRequest.of(0, 20));

    assertEquals(1, matching.getTotalElements());
    assertEquals(0, nonMatching.getTotalElements());
  }

  private Page<Campaign> findShared(final UUID toUserId, final PageRequest pageRequest) {
    return this.campaignRepository.findSharedCampaigns(
        toUserId, null, null, null, null, null, null, pageRequest);
  }

  private Campaign saveCampaignForOwner(final Campaign fixture, final UUID ownerId) {
    return this.campaignRepository.save(
        fixture.toBuilder().ownerId(ownerId).createdBy(ownerId).updatedBy(ownerId).build());
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
}
