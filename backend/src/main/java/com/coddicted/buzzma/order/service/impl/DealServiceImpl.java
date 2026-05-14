package com.coddicted.buzzma.order.service.impl;

import com.coddicted.buzzma.campaign.entity.Campaign;
import com.coddicted.buzzma.campaign.entity.CampaignStatus;
import com.coddicted.buzzma.campaign.entity.CampaignType;
import com.coddicted.buzzma.campaign.persistence.CampaignRepository;
import com.coddicted.buzzma.order.service.DealService;
import com.coddicted.buzzma.shared.common.BaseCrudService;
import com.coddicted.buzzma.shared.enums.Platform;
import com.coddicted.buzzma.shared.exception.NotFoundException;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DealServiceImpl extends BaseCrudService implements DealService {

  private static final Logger LOGGER = LoggerFactory.getLogger(DealServiceImpl.class);

  private final CampaignRepository campaignRepository;

  public DealServiceImpl(final CampaignRepository campaignRepository) {
    this.campaignRepository = campaignRepository;
  }

  @Override
  @Transactional(readOnly = true)
  public Page<Campaign> getDeals(
      final int page, final int size, final CampaignType type, final Platform platform) {
    LOGGER.debug(
        "Fetching active deals page={} size={} type={} platform={}", page, size, type, platform);
    return this.campaignRepository.findActiveDeals(
        CampaignStatus.CAMPAIGN_STATUS_ACTIVE, type, platform, PageRequest.of(page, size));
  }

  @Override
  @Transactional(readOnly = true)
  public Campaign getById(final UUID campaignId) {
    return this.campaignRepository
        .findByIdAndIsDeletedFalse(campaignId)
        .orElseThrow(
            () -> {
              LOGGER.warn("Campaign not found: {}", campaignId);
              return new NotFoundException("Campaign not found: " + campaignId);
            });
  }
}
