package com.coddicted.buzzma.campaign.service;

import com.coddicted.buzzma.campaign.entity.CampaignShare;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CampaignShareService {

  CampaignShare create(CampaignShare campaignShare);

  Optional<CampaignShare> findByCampaignId(UUID campaignId);

  List<CampaignShare> findByToUserId(UUID toUserId);

  boolean existsByCampaignId(UUID campaignId);

  boolean existsByCampaignIdAndToUserId(UUID campaignId, UUID toUserId);
}
