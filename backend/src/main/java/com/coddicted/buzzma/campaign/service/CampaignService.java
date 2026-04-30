package com.coddicted.buzzma.campaign.service;

import com.coddicted.buzzma.campaign.entity.Campaign;
import com.coddicted.buzzma.campaign.entity.CampaignAction;
import java.util.UUID;

public interface CampaignService {

    Campaign getById(UUID campaignId);

    Campaign create(Campaign campaign);

    Campaign update(Campaign campaign);

    Campaign delete(UUID campaignId, UUID requesterId);

    Campaign action(UUID campaignId, CampaignAction action, UUID requesterId);

    Campaign copy(UUID campaignId, UUID requesterId);
}
