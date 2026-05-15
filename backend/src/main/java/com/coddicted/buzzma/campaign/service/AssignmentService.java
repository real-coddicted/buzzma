package com.coddicted.buzzma.campaign.service;

import com.coddicted.buzzma.campaign.entity.CampaignAssignmentStatus;
import com.coddicted.buzzma.campaign.model.Assignment;

import java.math.BigInteger;
import java.util.Set;
import java.util.UUID;

public interface AssignmentService {

    Set<Assignment> getAssignments(final UUID assigneeId, final CampaignAssignmentStatus status);

    boolean publishAssignment(final UUID campaignId, final UUID campaignAssignmentId, final BigInteger commissionChargedPaise, final BigInteger dealPricePaise, final UUID chargedById);
}
