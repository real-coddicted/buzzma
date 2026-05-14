package com.coddicted.buzzma.order.service;

import com.coddicted.buzzma.campaign.entity.Campaign;
import com.coddicted.buzzma.campaign.entity.CampaignType;
import com.coddicted.buzzma.shared.enums.Platform;
import java.util.UUID;
import org.springframework.data.domain.Page;

public interface DealService {

  Page<Campaign> getDeals(int page, int size, CampaignType type, Platform platform);

  Campaign getById(UUID campaignId);
}
