package com.coddicted.buzzma.claim.service.impl;

import com.coddicted.buzzma.campaign.entity.Campaign;
import com.coddicted.buzzma.campaign.entity.CampaignAssignmentStatus;
import com.coddicted.buzzma.campaign.model.Assignment;
import com.coddicted.buzzma.campaign.service.AssignmentService;
import com.coddicted.buzzma.campaign.service.CampaignService;
import com.coddicted.buzzma.claim.entity.Claim;
import com.coddicted.buzzma.claim.model.ClaimReviewModel;
import com.coddicted.buzzma.claim.service.ClaimReviewService;
import com.coddicted.buzzma.claim.service.ClaimService;
import com.coddicted.buzzma.identity.entity.BuzzmaUser;
import com.coddicted.buzzma.identity.entity.UserRole;
import com.coddicted.buzzma.identity.service.UserService;
import com.coddicted.buzzma.shared.common.BaseCrudService;
import java.util.HashMap;
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
  private final CampaignService campaignService;
  private final UserService userService;

  public ClaimReviewServiceImpl(
      final ClaimService claimService,
      final AssignmentService assignmentService,
      final CampaignService campaignService,
      final UserService userService) {
    this.claimService = claimService;
    this.assignmentService = assignmentService;
    this.campaignService = campaignService;
    this.userService = userService;
  }

  @Override
  @Transactional(readOnly = true)
  public Page<ClaimReviewModel> getClaimReviews(final UUID requesterId, final Pageable pageable) {
    final BuzzmaUser requester = this.userService.getById(requesterId);
    final Map<UUID, Campaign> campaignById =
        getApplicableCampaignIds(requesterId, requester.getRole());
    return fetchClaims(campaignById, requester.getRole(), pageable);
  }

  private Map<UUID, Campaign> getApplicableCampaignIds(
      final UUID requesterId, final UserRole role) {
    final Set<Assignment> assignments =
        this.assignmentService.getAssignments(
            requesterId, CampaignAssignmentStatus.CAMPAIGN_ASSIGNMENT_STATUS_PUBLISHED);

    final Map<UUID, Campaign> campaignById =
        new HashMap<>(
            assignments.stream()
                .collect(
                    Collectors.toMap(
                        a -> a.getCampaign().getId(), Assignment::getCampaign, (a, b) -> a)));

    if (role == UserRole.ROLE_AGENCY || role == UserRole.ROLE_BRAND) {
      this.campaignService
          .getByOwnerId(requesterId)
          .forEach(s -> campaignById.putIfAbsent(s.getCampaign().getId(), s.getCampaign()));
    }

    return campaignById;
  }

  private Page<ClaimReviewModel> fetchClaims(
      final Map<UUID, Campaign> campaignById, final UserRole userRole, final Pageable pageable) {
    final List<UUID> campaignIds = List.copyOf(campaignById.keySet());

    if (campaignIds.isEmpty()) {
      LOGGER.info("No applicable campaigns found for role {}", userRole);
      return Page.empty(pageable);
    }

    final Page<Claim> claims = this.claimService.listClaimByCampaignIds(campaignIds, pageable);

    return claims.map(
        claim ->
            ClaimReviewModel.builder()
                .claim(claim)
                .campaign(campaignById.get(claim.getCampaignId()))
                .userRole(userRole)
                .build());
  }
}
