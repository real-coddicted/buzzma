package com.coddicted.buzzma.campaign.service;

import com.coddicted.buzzma.campaign.entity.CampaignType;
import com.coddicted.buzzma.campaign.entity.CampaignTypeStep;
import java.util.List;
import java.util.Map;

/**
 * @see com.coddicted.buzzma.campaign.entity.CampaignTypeStep
 */
@Deprecated
public interface CampaignTypeStepService {

  Map<CampaignType, List<CampaignTypeStep>> getStepConfig();
}
