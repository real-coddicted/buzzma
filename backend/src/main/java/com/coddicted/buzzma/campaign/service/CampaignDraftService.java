package com.coddicted.buzzma.campaign.service;

import com.coddicted.buzzma.campaign.dto.CampaignDraftRequestDto;
import com.coddicted.buzzma.campaign.dto.CampaignDraftResponseDto;
import com.coddicted.buzzma.campaign.entity.CampaignDraft;
import java.util.UUID;

public interface CampaignDraftService {

  CampaignDraftResponseDto create(UUID requesterId, CampaignDraftRequestDto request);

  CampaignDraftResponseDto update(UUID requesterId, UUID id, CampaignDraftRequestDto request);

  CampaignDraftResponseDto getById(UUID id);

  CampaignDraft getEntityById(UUID id);

  CampaignDraftResponseDto copyFromCampaign(UUID campaignId, UUID requesterId);

  void delete(UUID requesterId, UUID id);
}
