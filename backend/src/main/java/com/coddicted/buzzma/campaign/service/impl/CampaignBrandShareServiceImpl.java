package com.coddicted.buzzma.campaign.service.impl;

import com.coddicted.buzzma.campaign.entity.CampaignBrandShare;
import com.coddicted.buzzma.campaign.persistence.CampaignBrandShareRepository;
import com.coddicted.buzzma.campaign.service.CampaignBrandShareService;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CampaignBrandShareServiceImpl implements CampaignBrandShareService {

  private final CampaignBrandShareRepository campaignBrandShareRepository;

  public CampaignBrandShareServiceImpl(
      final CampaignBrandShareRepository campaignBrandShareRepository) {
    this.campaignBrandShareRepository = campaignBrandShareRepository;
  }

  @Override
  @Transactional
  public CampaignBrandShare create(final CampaignBrandShare campaignBrandShare) {
    return this.campaignBrandShareRepository.save(campaignBrandShare);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<CampaignBrandShare> findByCampaignId(final UUID campaignId) {
    return this.campaignBrandShareRepository.findByCampaignId(campaignId);
  }

  @Override
  @Transactional(readOnly = true)
  public List<CampaignBrandShare> findByBrandUserId(final UUID brandUserId) {
    return this.campaignBrandShareRepository.findByBrandUserId(brandUserId);
  }

  @Override
  @Transactional(readOnly = true)
  public boolean existsByCampaignId(final UUID campaignId) {
    return this.campaignBrandShareRepository.existsByCampaignId(campaignId);
  }
}
