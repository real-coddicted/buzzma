package com.coddicted.buzzma.campaign.service;

import com.coddicted.buzzma.campaign.api.CampaignRequestDto;
import com.coddicted.buzzma.campaign.api.CampaignResponseDto;

public interface CampaignService {

  CampaignResponseDto create(CampaignRequestDto request);
}
