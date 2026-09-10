package com.coddicted.buzzma.campaign.service;

import com.coddicted.buzzma.campaign.dto.CampaignDraftResponseDto;
import com.coddicted.buzzma.campaign.dto.CreateDraftRequestDto;
import com.coddicted.buzzma.campaign.dto.UpdateDraftRequestDto;
import com.coddicted.buzzma.campaign.entity.CampaignDraft;
import java.util.UUID;

public interface CampaignDraftService {

  CampaignDraftResponseDto create(UUID requesterId, CreateDraftRequestDto request);

  CampaignDraftResponseDto update(UUID requesterId, UUID id, UpdateDraftRequestDto request);

  CampaignDraftResponseDto getById(UUID id);

  CampaignDraft getEntityById(UUID id);

  CampaignDraftResponseDto copyFromCampaign(UUID campaignId, UUID requesterId);

  void delete(UUID requesterId, UUID id);
}
