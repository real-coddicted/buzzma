package com.coddicted.buzzma.campaign.service.impl;

import com.coddicted.buzzma.campaign.api.CampaignRequestDto;
import com.coddicted.buzzma.campaign.api.CampaignResponseDto;
import com.coddicted.buzzma.campaign.mapper.CampaignMapper;
import com.coddicted.buzzma.campaign.entity.Campaign;
import com.coddicted.buzzma.campaign.persistence.CampaignRepository;
import com.coddicted.buzzma.campaign.service.CampaignService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CampaignServiceImpl implements CampaignService {

  private final CampaignRepository repository;
  private final CampaignMapper mapper;

  public CampaignServiceImpl(CampaignRepository repository, CampaignMapper mapper) {
    this.repository = repository;
    this.mapper = mapper;
  }

  @Override
  @Transactional
  public CampaignResponseDto create(CampaignRequestDto request) {
    Campaign entity = mapper.toCampaignEntity(request);
    return mapper.toResponse(repository.save(entity));
  }
}
