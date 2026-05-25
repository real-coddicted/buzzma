package com.coddicted.buzzma.claim.service.impl;

import com.coddicted.buzzma.campaign.entity.Campaign;
import com.coddicted.buzzma.campaign.entity.CampaignAssignmentStatus;
import com.coddicted.buzzma.campaign.model.Assignment;
import com.coddicted.buzzma.campaign.service.AssignmentService;
import com.coddicted.buzzma.claim.entity.Claim;
import com.coddicted.buzzma.claim.model.ClaimReviewModel;
import com.coddicted.buzzma.claim.service.ClaimReviewService;
import com.coddicted.buzzma.claim.service.ClaimService;
import com.coddicted.buzzma.shared.common.BaseCrudService;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ClaimReviewServiceImpl extends BaseCrudService implements ClaimReviewService {

  private static final Logger LOGGER = LoggerFactory.getLogger(ClaimReviewServiceImpl.class);

  private final ClaimService claimService;
  private final AssignmentService assignmentService;

  public ClaimReviewServiceImpl(
      final ClaimService claimService, final AssignmentService assignmentService) {
    this.claimService = claimService;
    this.assignmentService = assignmentService;
  }

  @Override
  @Transactional(readOnly = true)
  public Page<ClaimReviewModel> getClaimReviews(final UUID requesterId, final Pageable pageable) {
    final Set<Assignment> assignments =
        this.assignmentService.getAssignments(
            requesterId, CampaignAssignmentStatus.CAMPAIGN_ASSIGNMENT_STATUS_PUBLISHED);

    final Map<UUID, Campaign> campaignById =
        assignments.stream()
            .collect(
                Collectors.toMap(
                    a -> a.getCampaign().getId(), Assignment::getCampaign, (a, b) -> a));

    final List<UUID> campaignIds = new ArrayList<>(campaignById.keySet());

    if (campaignIds.isEmpty()) {
      LOGGER.debug("No published assignments found for requester {}", requesterId);
      return Page.empty(pageable);
    }

    final Page<Claim> claims = this.claimService.listClaimByCampaignIds(campaignIds, pageable);

    return claims.map(
        claim ->
            ClaimReviewModel.builder()
                .claim(claim)
                .campaign(campaignById.get(claim.getCampaignId()))
                .build());
  }
}
