package com.coddicted.buzzma.campaign.service;

import com.coddicted.buzzma.campaign.dto.CampaignStepDto;
import java.util.List;
import java.util.Map;

public interface CampaignTypeStepService {

  Map<String, List<CampaignStepDto>> getStepConfig();
}
