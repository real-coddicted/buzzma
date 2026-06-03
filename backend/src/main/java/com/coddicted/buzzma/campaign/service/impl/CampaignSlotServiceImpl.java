package com.coddicted.buzzma.campaign.service.impl;

import com.coddicted.buzzma.campaign.entity.CampaignSlot;
import com.coddicted.buzzma.campaign.persistence.CampaignSlotRepository;
import com.coddicted.buzzma.campaign.service.CampaignSlotService;
import com.coddicted.buzzma.shared.common.BaseCrudService;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class CampaignSlotServiceImpl extends BaseCrudService implements CampaignSlotService {

  private final CampaignSlotRepository campaignSlotRepository;

  public CampaignSlotServiceImpl(final CampaignSlotRepository campaignSlotRepository) {
    this.campaignSlotRepository = campaignSlotRepository;
  }

  @Override
  public List<CampaignSlot> getByCampaignIds(final List<UUID> campaignIds) {
    return this.campaignSlotRepository.findByCampaignIdInAndIsDeletedFalse(campaignIds);
  }

  @Override
  public int decrementSlot(final UUID campaignSlotId) {
    return this.campaignSlotRepository.decrementSlotsAvailableIfPositive(campaignSlotId);
  }

  @Override
  public CampaignSlot create(final CampaignSlot campaignSlot) {
    return this.campaignSlotRepository.save(campaignSlot);
  }

  @Override
  public List<CampaignSlot> create(final List<CampaignSlot> campaignSlots) {
    return this.campaignSlotRepository.saveAll(campaignSlots);
  }
}
