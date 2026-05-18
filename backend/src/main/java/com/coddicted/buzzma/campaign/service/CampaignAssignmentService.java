package com.coddicted.buzzma.campaign.service;

import com.coddicted.buzzma.campaign.entity.CampaignAssignment;
import com.coddicted.buzzma.campaign.entity.CampaignAssignmentStatus;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CampaignAssignmentService {
  List<CampaignAssignment> listAssignmentsByAssignor(UUID assignorId);

  List<CampaignAssignment> listAssignmentsByAssignee(
      UUID assigneeId, CampaignAssignmentStatus campaignAssignmentStatus);

  Page<CampaignAssignment> listAssignmentsByAssignee(
      UUID assigneeId, CampaignAssignmentStatus status, Pageable pageable);

  List<CampaignAssignment> create(List<CampaignAssignment> assignees);

  List<CampaignAssignment> update(List<CampaignAssignment> assignees);

  CampaignAssignment delete(UUID campaignAssignmentId, UUID requesterId);

  List<CampaignAssignment> copy(
      List<UUID> srcCampaignAssignments, UUID destCampaignId, UUID requesterId);

  List<CampaignAssignment> lockAssignments(List<UUID> campaignAssignments);

  CampaignAssignment getById(UUID campaignAssignmentId);
}
