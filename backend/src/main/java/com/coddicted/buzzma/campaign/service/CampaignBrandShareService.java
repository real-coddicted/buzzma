package com.coddicted.buzzma.campaign.service;

import com.coddicted.buzzma.campaign.entity.CampaignBrandShare;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CampaignBrandShareService {

  CampaignBrandShare create(CampaignBrandShare campaignBrandShare);

  Optional<CampaignBrandShare> findByCampaignId(UUID campaignId);

  List<CampaignBrandShare> findByBrandUserId(UUID brandUserId);

  boolean existsByCampaignId(UUID campaignId);
}
