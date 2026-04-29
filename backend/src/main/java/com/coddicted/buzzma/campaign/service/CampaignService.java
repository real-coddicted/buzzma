package com.coddicted.buzzma.campaign.service;

import com.coddicted.buzzma.campaign.api.CampaignRequestDto;
import com.coddicted.buzzma.campaign.api.CampaignResponseDto;

import java.util.UUID;

public interface CampaignService {

    CampaignResponseDto getById(UUID campaignId);

    CampaignResponseDto create(CampaignRequestDto request);

    CampaignResponseDto update(UUID campaignId, CampaignRequestDto request);

    CampaignResponseDto delete(UUID campaignId);

    CampaignResponseDto publish(UUID campaignId);

    CampaignResponseDto copy(UUID campaignId);
}
