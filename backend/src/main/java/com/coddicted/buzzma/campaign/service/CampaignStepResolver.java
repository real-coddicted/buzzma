package com.coddicted.buzzma.campaign.service;

import com.coddicted.buzzma.campaign.entity.Campaign;
import com.coddicted.buzzma.campaign.entity.CampaignStepType;
import java.util.List;

public interface CampaignStepResolver {

  /** The claim steps for this campaign, in submission order, with CASHBACK always last. */
  List<CampaignStepType> resolve(Campaign campaign);
}
