package com.coddicted.buzzma.campaign.service;

import com.coddicted.buzzma.campaign.api.CampaignAssignmentRequestDto;
import com.coddicted.buzzma.campaign.api.CampaignAssignmentResponseDto;
import com.coddicted.buzzma.campaign.api.CampaignRequestDto;
import com.coddicted.buzzma.campaign.api.CampaignResponseDto;

import java.util.List;
import java.util.UUID;

public interface CampaignService {

  CampaignResponseDto getById(UUID id);
  CampaignResponseDto create(CampaignRequestDto request);
  CampaignResponseDto update(UUID id, CampaignRequestDto request);
  CampaignResponseDto delete(UUID id);
}
