package com.coddicted.buzzma.campaign.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import com.coddicted.buzzma.campaign.entity.CampaignShare;
import com.coddicted.buzzma.campaign.persistence.CampaignShareRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CampaignShareServiceImplTest {

  private static final UUID CAMPAIGN_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
  private static final UUID BRAND_USER_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");

  @Mock private CampaignShareRepository mockCampaignShareRepository;
  private CampaignShareServiceImpl campaignShareService;

  @BeforeEach
  void setUp() {
    this.campaignShareService = new CampaignShareServiceImpl(this.mockCampaignShareRepository);
  }

  @Test
  void testCreate() {
    final CampaignShare share =
        CampaignShare.builder().campaignId(CAMPAIGN_ID).toUserId(BRAND_USER_ID).build();
    when(this.mockCampaignShareRepository.save(share)).thenReturn(share);

    final CampaignShare result = this.campaignShareService.create(share);

    assertEquals(share, result);
  }

  @Test
  void testFindByCampaignId() {
    final CampaignShare share =
        CampaignShare.builder().campaignId(CAMPAIGN_ID).toUserId(BRAND_USER_ID).build();
    when(this.mockCampaignShareRepository.findByCampaignId(CAMPAIGN_ID))
        .thenReturn(Optional.of(share));

    final Optional<CampaignShare> result = this.campaignShareService.findByCampaignId(CAMPAIGN_ID);

    assertEquals(Optional.of(share), result);
  }

  @Test
  void testFindByBrandUserId() {
    final CampaignShare share =
        CampaignShare.builder().campaignId(CAMPAIGN_ID).toUserId(BRAND_USER_ID).build();
    when(this.mockCampaignShareRepository.findByToUserId(BRAND_USER_ID)).thenReturn(List.of(share));

    final List<CampaignShare> result = this.campaignShareService.findByToUserId(BRAND_USER_ID);

    assertEquals(List.of(share), result);
  }

  @Test
  void testExistsByCampaignId() {
    when(this.mockCampaignShareRepository.existsByCampaignId(CAMPAIGN_ID)).thenReturn(true);
    when(this.mockCampaignShareRepository.existsByCampaignId(BRAND_USER_ID)).thenReturn(false);

    assertTrue(this.campaignShareService.existsByCampaignId(CAMPAIGN_ID));
    assertFalse(this.campaignShareService.existsByCampaignId(BRAND_USER_ID));
  }
}
