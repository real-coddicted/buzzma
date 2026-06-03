package com.coddicted.buzzma.campaign.service;

import com.coddicted.buzzma.campaign.entity.CampaignSlot;
import java.util.List;
import java.util.UUID;

public interface CampaignSlotService {
  List<CampaignSlot> getByCampaignIds(List<UUID> campaignId);

  int decrementSlot(UUID campaignSlotId);

  CampaignSlot create(CampaignSlot campaignSlot);

  List<CampaignSlot> create(List<CampaignSlot> campaignSlots);
}
