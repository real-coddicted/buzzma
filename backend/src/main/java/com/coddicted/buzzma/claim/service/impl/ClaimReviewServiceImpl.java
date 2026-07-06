package com.coddicted.buzzma.claim.service.impl;

import com.coddicted.buzzma.campaign.entity.Campaign;
import com.coddicted.buzzma.campaign.model.CampaignSummary;
import com.coddicted.buzzma.campaign.service.CampaignService;
import com.coddicted.buzzma.claim.model.ClaimReviewModel;
import com.coddicted.buzzma.claim.service.ClaimReviewService;
import com.coddicted.buzzma.claim.service.ClaimService;
import com.coddicted.buzzma.identity.entity.BuzzmaUser;
import com.coddicted.buzzma.identity.entity.UserRole;
import com.coddicted.buzzma.shared.common.BaseCrudService;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ClaimReviewServiceImpl extends BaseCrudService implements ClaimReviewService {

  private static final Logger LOGGER = LoggerFactory.getLogger(ClaimReviewServiceImpl.class);

  private final ClaimService claimService;
  private final CampaignService campaignService;

  public ClaimReviewServiceImpl(
      final ClaimService claimService, final CampaignService campaignService) {
    this.claimService = claimService;
    this.campaignService = campaignService;
  }

  @Override
  @Transactional(readOnly = true)
  public Page<ClaimReviewModel> getClaimReviews(
      final BuzzmaUser requester, final Pageable pageable) {
    // updatedAt-descending ordering is a business rule enforced in the repository queries
    // themselves, so any client-supplied sort is stripped here to avoid a conflicting ORDER BY.
    final Pageable unsortedPageable =
        PageRequest.of(pageable.getPageNumber(), pageable.getPageSize());

    if (requester.getRole() == UserRole.ROLE_MEDIATOR) {
      return this.claimService.findClaimsToReviewForMediator(requester.getId(), unsortedPageable);
    }

    final List<UUID> campaignIds = List.copyOf(getApplicableCampaignIds(requester.getId()));
    if (campaignIds.isEmpty()) {
      LOGGER.info("No applicable campaigns found for role {}", requester.getRole());
      return Page.empty(unsortedPageable);
    }

    return this.claimService.findClaimsToReviewForCampaigns(campaignIds, unsortedPageable);
  }

  private Set<UUID> getApplicableCampaignIds(final UUID requesterId) {
    return this.campaignService.getByOwnerId(requesterId).stream()
        .map(CampaignSummary::getCampaign)
        .map(Campaign::getId)
        .collect(Collectors.toSet());
  }
}
