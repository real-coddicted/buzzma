package com.coddicted.buzzma.campaign.service;

import com.coddicted.buzzma.campaign.api.CampaignAssignmentRequestDto;
import com.coddicted.buzzma.campaign.api.CampaignAssignmentResponseDto;

import java.util.List;
import java.util.UUID;

public interface CampaignAssignmentService {
    List<CampaignAssignmentResponseDto> listAssignmentsByAssignor(UUID assignorId);
    List<CampaignAssignmentResponseDto> listAssignmentsByAssignee(UUID assigneeId);
    List<CampaignAssignmentRequestDto> create(List<CampaignAssignmentRequestDto> assignees);
    List<CampaignAssignmentRequestDto> update(List<CampaignAssignmentRequestDto> assignees);
    CampaignAssignmentRequestDto delete(CampaignAssignmentRequestDto assignee);
}
