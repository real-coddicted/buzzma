package com.coddicted.buzzma.campaign.service;

import com.coddicted.buzzma.campaign.entity.Campaign;
import com.coddicted.buzzma.campaign.entity.CampaignAction;
import com.coddicted.buzzma.campaign.model.CampaignSearchCriteria;
import com.coddicted.buzzma.campaign.model.CampaignSummary;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CampaignService {

  Campaign getById(UUID campaignId);

  Campaign create(Campaign campaign);

  Campaign update(Campaign campaign);

  Campaign delete(UUID campaignId, UUID requesterId);

  Campaign action(UUID campaignId, CampaignAction action, UUID requesterId);

  Set<Campaign> findCampaignsById(Set<UUID> campaignIdSet);

  Campaign copy(UUID campaignId, UUID requesterId);

  List<CampaignSummary> getByOwnerId(UUID ownerId);

  Page<CampaignSummary> getByOwnerId(UUID ownerId, Pageable pageable);

  Page<CampaignSummary> search(UUID ownerId, CampaignSearchCriteria criteria, Pageable pageable);

  List<String> getBrandNames(UUID ownerId);
}
