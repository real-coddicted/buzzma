package com.coddicted.buzzma.campaign.service;

import com.coddicted.buzzma.campaign.dto.AssignmentSummaryResponseDto;
import com.coddicted.buzzma.campaign.entity.CampaignAssignmentStatus;
import com.coddicted.buzzma.campaign.model.Assignment;
import java.math.BigInteger;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AssignmentService {

  Assignment getAssignmentById(UUID id, UUID requesterId);

  Set<Assignment> getAssignments(final UUID assigneeId, final CampaignAssignmentStatus status);

  Page<AssignmentSummaryResponseDto> getAssignmentSummaries(
      UUID assigneeId, CampaignAssignmentStatus status, Pageable pageable);

  boolean publishAssignment(
      final UUID campaignId,
      final UUID campaignAssignmentId,
      final BigInteger commissionChargedPaise,
      final BigInteger dealPricePaise,
      final UUID chargedById,
      final String affiliateUrl);
}
