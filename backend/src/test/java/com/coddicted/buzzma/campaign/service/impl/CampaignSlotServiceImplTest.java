package com.coddicted.buzzma.campaign.service.impl;

import static com.coddicted.buzzma.campaign.service.impl.Fixtures.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.coddicted.buzzma.campaign.entity.CampaignSlot;
import com.coddicted.buzzma.campaign.persistence.CampaignSlotRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CampaignSlotServiceImplTest {

  @Mock private CampaignSlotRepository mockCampaignSlotRepository;

  private CampaignSlotServiceImpl campaignSlotService;

  @BeforeEach
  void setUp() {
    this.campaignSlotService = new CampaignSlotServiceImpl(this.mockCampaignSlotRepository);
  }

  @Test
  void testGetByCampaignIds() {
    final List<java.util.UUID> campaignIds = List.of(CAMPAIGN_ID_1, CAMPAIGN_ID_2);
    when(this.mockCampaignSlotRepository.findByCampaignIdInAndIsDeletedFalse(campaignIds))
        .thenReturn(List.of(SLOT_1, SLOT_2));

    final List<CampaignSlot> result = this.campaignSlotService.getByCampaignIds(campaignIds);

    assertEquals(List.of(SLOT_1, SLOT_2), result);
  }

  @Test
  void testDecrementSlotWhenAvailable() {
    when(this.mockCampaignSlotRepository.decrementSlotsAvailableIfPositive(SLOT_ID_1))
        .thenReturn(1);

    final int result = this.campaignSlotService.decrementSlot(SLOT_ID_1);

    assertEquals(1, result);
    verify(this.mockCampaignSlotRepository).decrementSlotsAvailableIfPositive(SLOT_ID_1);
  }

  @Test
  void testDecrementSlotWhenNoneAvailable() {
    when(this.mockCampaignSlotRepository.decrementSlotsAvailableIfPositive(SLOT_ID_1))
        .thenReturn(0);

    final int result = this.campaignSlotService.decrementSlot(SLOT_ID_1);

    assertEquals(0, result);
  }

  @Test
  void testCreateSingle() {
    when(this.mockCampaignSlotRepository.save(SLOT_1)).thenReturn(SLOT_1);

    final CampaignSlot result = this.campaignSlotService.create(SLOT_1);

    assertEquals(SLOT_1, result);
    verify(this.mockCampaignSlotRepository).save(SLOT_1);
  }

  @Test
  void testCreateList() {
    final List<CampaignSlot> slots = List.of(SLOT_1, SLOT_2);
    when(this.mockCampaignSlotRepository.saveAll(slots)).thenReturn(slots);

    final List<CampaignSlot> result = this.campaignSlotService.create(slots);

    assertEquals(slots, result);
    verify(this.mockCampaignSlotRepository).saveAll(slots);
  }
}
