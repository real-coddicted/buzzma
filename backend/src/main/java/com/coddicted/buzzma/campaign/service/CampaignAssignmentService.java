package com.coddicted.buzzma.campaign.service;

import com.coddicted.buzzma.campaign.api.CampaignAssignmentRequestDto;
import com.coddicted.buzzma.campaign.api.CampaignAssignmentResponseDto;
import com.coddicted.buzzma.campaign.entity.AssigneeType;
import com.coddicted.buzzma.campaign.entity.AssignorType;

import java.util.List;

public interface CampaignAssignmentService {
    List<CampaignAssignmentResponseDto> listAssignmentsByAssignor(AssignorType assignorType, String assignorCode);
    List<CampaignAssignmentResponseDto> listAssignmentsByAssignee(AssigneeType assigneeType, String assigneeCode);
    List<CampaignAssignmentRequestDto> create(List<CampaignAssignmentRequestDto> assignees);
    List<CampaignAssignmentRequestDto> update(List<CampaignAssignmentRequestDto> assignees);
    CampaignAssignmentRequestDto delete(CampaignAssignmentRequestDto assignee);
}
