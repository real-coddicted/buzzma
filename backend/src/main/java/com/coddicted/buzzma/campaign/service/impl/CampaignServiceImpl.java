package com.coddicted.buzzma.campaign.service.impl;

import com.coddicted.buzzma.campaign.api.*;
import com.coddicted.buzzma.campaign.entity.Campaign;
import com.coddicted.buzzma.campaign.entity.CampaignAssignment;
import com.coddicted.buzzma.campaign.mapper.CampaignAssignmentMapper;
import com.coddicted.buzzma.campaign.mapper.CampaignMapper;
import com.coddicted.buzzma.campaign.persistence.CampaignAssignmentRepository;
import com.coddicted.buzzma.campaign.persistence.CampaignRepository;
import com.coddicted.buzzma.campaign.service.CampaignService;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CampaignServiceImpl implements CampaignService {

    private final CampaignMapper campaignMapper;
    private final CampaignAssignmentMapper campaignAssignmentMapper;
    private final CampaignRepository campaignRepository;
    private final CampaignAssignmentRepository campaignAssignmentRepository;

    public CampaignServiceImpl(final CampaignMapper campaignMapper,
                               final CampaignAssignmentMapper campaignAssignmentMapper,
                               final CampaignRepository campaignRepository,
                               final CampaignAssignmentRepository campaignAssignmentRepository) {
        this.campaignMapper = campaignMapper;
        this.campaignAssignmentMapper = campaignAssignmentMapper;
        this.campaignRepository = campaignRepository;
        this.campaignAssignmentRepository = campaignAssignmentRepository;
    }

    @Override
    @Transactional
    public CampaignResponseDto create(final CampaignRequestDto request) {
        final Campaign campaign = campaignMapper.toCampaignEntity(request);
        campaignRepository.save(campaign);
        return campaignMapper.toResponse(campaign);
    }
}
