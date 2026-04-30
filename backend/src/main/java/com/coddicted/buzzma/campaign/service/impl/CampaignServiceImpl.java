package com.coddicted.buzzma.campaign.service.impl;

import com.coddicted.buzzma.campaign.api.*;
import com.coddicted.buzzma.campaign.entity.Campaign;
import com.coddicted.buzzma.campaign.entity.CampaignAction;
import com.coddicted.buzzma.campaign.entity.CampaignAssignment;
import com.coddicted.buzzma.campaign.entity.CampaignAssignmentStatus;
import com.coddicted.buzzma.campaign.entity.CampaignStatus;
import com.coddicted.buzzma.campaign.mapper.CampaignAssignmentMapper;
import com.coddicted.buzzma.campaign.mapper.CampaignMapper;
import com.coddicted.buzzma.campaign.persistence.CampaignAssignmentRepository;
import com.coddicted.buzzma.campaign.persistence.CampaignRepository;
import com.coddicted.buzzma.campaign.service.CampaignService;
import com.coddicted.buzzma.campaign.service.CampaignStateMachine;
import com.coddicted.buzzma.shared.exception.ApiException;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CampaignServiceImpl implements CampaignService {

    private final CampaignMapper campaignMapper;
    private final CampaignAssignmentMapper campaignAssignmentMapper;
    private final CampaignRepository campaignRepository;
    private final CampaignAssignmentRepository campaignAssignmentRepository;
    private final CampaignStateMachine stateMachine;

    public CampaignServiceImpl(final CampaignMapper campaignMapper,
                               final CampaignAssignmentMapper campaignAssignmentMapper,
                               final CampaignRepository campaignRepository,
                               final CampaignAssignmentRepository campaignAssignmentRepository,
                               final CampaignStateMachine stateMachine) {
        this.campaignMapper = campaignMapper;
        this.campaignAssignmentMapper = campaignAssignmentMapper;
        this.campaignRepository = campaignRepository;
        this.campaignAssignmentRepository = campaignAssignmentRepository;
        this.stateMachine = stateMachine;
    }

    @Override
    public CampaignResponseDto getById(UUID campaignId) {
        return null;
    }

    @Override
    @Transactional
    public CampaignResponseDto create(final CampaignRequestDto request) {
        final Campaign campaign = campaignMapper.toCampaignEntity(request);
        campaignRepository.save(campaign);
        return campaignMapper.toResponse(campaign);
    }

    @Override
    public CampaignResponseDto update(UUID campaignId, CampaignRequestDto request) {
        return null;
    }

    @Override
    public CampaignResponseDto delete(UUID campaignId) {
        return null;
    }

    @Override
    @Transactional
    public CampaignResponseDto action(final UUID campaignId, final CampaignAction campaignAction, final UUID requesterId) {
        CampaignStatus status = CampaignStatus.CAMPAIGN_STATUS_DRAFT;
        switch(campaignAction){
            case CAMPAIGN_ACTION_PUBLISH, CAMPAIGN_ACTION_RESUME -> status = CampaignStatus.CAMPAIGN_STATUS_ACTIVE;
            case CAMPAIGN_ACTION_PAUSE -> status = CampaignStatus.CAMPAIGN_STATUS_PAUSED;
            case CAMPAIGN_ACTION_CLOSE -> status = CampaignStatus.CAMPAIGN_STATUS_CLOSED;
            case CAMPAIGN_ACTION_COMPLETE -> status = CampaignStatus.CAMPAIGN_STATUS_COMPLETED;
        }
        return transitionTo(campaignId, status, requesterId);
    }

    @Override
    public CampaignResponseDto copy(final UUID campaignId) {
        return null;
    }

    private CampaignResponseDto transitionTo(final UUID campaignId, final CampaignStatus target, final UUID requesterId) {
        final Campaign campaign = campaignRepository.findById(campaignId)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "CAMPAIGN_NOT_FOUND"));
        final boolean isPublish = campaign.getStatus() == CampaignStatus.CAMPAIGN_STATUS_DRAFT
            && target == CampaignStatus.CAMPAIGN_STATUS_ACTIVE;
        if (isPublish) {
            if (!campaign.getOwnerId().equals(requesterId)) {
                throw new ApiException(HttpStatus.FORBIDDEN, "NOT_CAMPAIGN_OWNER");
            }
            final List<CampaignAssignment> assignments = campaignAssignmentRepository.findByCampaignId(campaignId);
            if (assignments.isEmpty()) {
                throw new ApiException(HttpStatus.UNPROCESSABLE_ENTITY, "NO_CAMPAIGN_ASSIGNMENTS");
            }
            stateMachine.transition(campaign, target);
            assignments.forEach(a -> a.setStatus(CampaignAssignmentStatus.CAMPAIGN_ASSIGNMENT_STATUS_LOCKED));
            campaignAssignmentRepository.saveAll(assignments);
        } else {
            stateMachine.transition(campaign, target);
        }
        return campaignMapper.toResponse(campaignRepository.save(campaign));
    }


}
