package com.coddicted.buzzma.campaign.service.impl;

import com.coddicted.buzzma.campaign.entity.CampaignShare;
import com.coddicted.buzzma.campaign.persistence.CampaignShareRepository;
import com.coddicted.buzzma.campaign.service.CampaignShareService;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CampaignShareServiceImpl implements CampaignShareService {

  private final CampaignShareRepository campaignShareRepository;

  public CampaignShareServiceImpl(final CampaignShareRepository campaignShareRepository) {
    this.campaignShareRepository = campaignShareRepository;
  }

  @Override
  @Transactional
  public CampaignShare create(final CampaignShare campaignShare) {
    return this.campaignShareRepository.save(campaignShare);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<CampaignShare> findByCampaignId(final UUID campaignId) {
    return this.campaignShareRepository.findByCampaignId(campaignId);
  }

  @Override
  @Transactional(readOnly = true)
  public List<CampaignShare> findByToUserId(final UUID toUserId) {
    return this.campaignShareRepository.findByToUserId(toUserId);
  }

  @Override
  @Transactional(readOnly = true)
  public boolean existsByCampaignId(final UUID campaignId) {
    return this.campaignShareRepository.existsByCampaignId(campaignId);
  }
}
