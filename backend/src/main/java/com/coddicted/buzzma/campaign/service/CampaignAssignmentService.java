package com.coddicted.buzzma.campaign.service;

import com.coddicted.buzzma.campaign.entity.CampaignAssignment;

import java.util.List;
import java.util.UUID;

public interface CampaignAssignmentService {
    List<CampaignAssignment> listAssignmentsByAssignor(UUID assignorId);

    List<CampaignAssignment> listAssignmentsByAssignee(UUID assigneeId);

    List<CampaignAssignment> create(List<CampaignAssignment> assignees);

    List<CampaignAssignment> update(List<CampaignAssignment> assignees);

    CampaignAssignment delete(UUID campaignAssignmentId, UUID requesterId);

    List<CampaignAssignment> copy(List<UUID> srcCampaignAssignments, UUID destCampaignId, UUID requesterId);

    List<CampaignAssignment> lockAssignments(List<UUID> campaignAssignments);

}
