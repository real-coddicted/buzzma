package com.coddicted.buzzma.campaign.service;

import com.coddicted.buzzma.campaign.entity.CampaignAssignmentStatus;
import com.coddicted.buzzma.campaign.model.Assignment;
import java.math.BigInteger;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AssignmentService {

  Set<Assignment> getAssignments(final UUID assigneeId, final CampaignAssignmentStatus status);

  Page<Assignment> getAssignments(
      UUID assigneeId, CampaignAssignmentStatus status, Pageable pageable);

  boolean publishAssignment(
      final UUID campaignId,
      final UUID campaignAssignmentId,
      final BigInteger commissionChargedPaise,
      final BigInteger dealPricePaise,
      final UUID chargedById);
}
