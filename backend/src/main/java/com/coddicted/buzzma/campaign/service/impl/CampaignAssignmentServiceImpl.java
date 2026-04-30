package com.coddicted.buzzma.campaign.service.impl;

import com.coddicted.buzzma.campaign.api.CampaignAssignmentRequestDto;
import com.coddicted.buzzma.campaign.api.CampaignAssignmentResponseDto;
import com.coddicted.buzzma.campaign.service.CampaignAssignmentService;

import java.util.List;
import java.util.UUID;

public class CampaignAssignmentServiceImpl implements CampaignAssignmentService {

    @Override
    public List<CampaignAssignmentResponseDto> listAssignmentsByAssignor(UUID assignorId) {
        return List.of();
    }

    @Override
    public List<CampaignAssignmentResponseDto> listAssignmentsByAssignee(UUID assigneeId) {
        return List.of();
    }

    @Override
    public List<CampaignAssignmentResponseDto> create(List<CampaignAssignmentRequestDto> assignees) {
        return List.of();
    }

    @Override
    public List<CampaignAssignmentResponseDto> update(List<CampaignAssignmentRequestDto> assignees) {
        return List.of();
    }

    @Override
    public CampaignAssignmentRequestDto delete(CampaignAssignmentRequestDto assignee) {
        return null;
    }

    @Override
    public List<CampaignAssignmentResponseDto> copy(List<UUID> srcCampaignAssignments, UUID destCampaignId) {
        return List.of();
    }

    @Override
    public List<CampaignAssignmentResponseDto> lockAssignments(List<UUID> campaignAssignments) {
        return List.of();
    }
}
