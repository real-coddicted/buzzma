package com.coddicted.buzzma.campaign.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import com.coddicted.buzzma.campaign.entity.CampaignBrandShare;
import com.coddicted.buzzma.campaign.persistence.CampaignBrandShareRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CampaignBrandShareServiceImplTest {

  private static final UUID CAMPAIGN_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
  private static final UUID BRAND_USER_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");

  @Mock private CampaignBrandShareRepository mockCampaignBrandShareRepository;
  private CampaignBrandShareServiceImpl campaignBrandShareService;

  @BeforeEach
  void setUp() {
    this.campaignBrandShareService =
        new CampaignBrandShareServiceImpl(this.mockCampaignBrandShareRepository);
  }

  @Test
  void testCreate() {
    final CampaignBrandShare share =
        CampaignBrandShare.builder().campaignId(CAMPAIGN_ID).brandUserId(BRAND_USER_ID).build();
    when(this.mockCampaignBrandShareRepository.save(share)).thenReturn(share);

    final CampaignBrandShare result = this.campaignBrandShareService.create(share);

    assertEquals(share, result);
  }

  @Test
  void testFindByCampaignId() {
    final CampaignBrandShare share =
        CampaignBrandShare.builder().campaignId(CAMPAIGN_ID).brandUserId(BRAND_USER_ID).build();
    when(this.mockCampaignBrandShareRepository.findByCampaignId(CAMPAIGN_ID))
        .thenReturn(Optional.of(share));

    final Optional<CampaignBrandShare> result =
        this.campaignBrandShareService.findByCampaignId(CAMPAIGN_ID);

    assertEquals(Optional.of(share), result);
  }

  @Test
  void testFindByBrandUserId() {
    final CampaignBrandShare share =
        CampaignBrandShare.builder().campaignId(CAMPAIGN_ID).brandUserId(BRAND_USER_ID).build();
    when(this.mockCampaignBrandShareRepository.findByBrandUserId(BRAND_USER_ID))
        .thenReturn(List.of(share));

    final List<CampaignBrandShare> result =
        this.campaignBrandShareService.findByBrandUserId(BRAND_USER_ID);

    assertEquals(List.of(share), result);
  }

  @Test
  void testExistsByCampaignId() {
    when(this.mockCampaignBrandShareRepository.existsByCampaignId(CAMPAIGN_ID)).thenReturn(true);
    when(this.mockCampaignBrandShareRepository.existsByCampaignId(BRAND_USER_ID)).thenReturn(false);

    assertTrue(this.campaignBrandShareService.existsByCampaignId(CAMPAIGN_ID));
    assertFalse(this.campaignBrandShareService.existsByCampaignId(BRAND_USER_ID));
  }
}
