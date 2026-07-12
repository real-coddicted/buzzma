package com.coddicted.buzzma.claim.service.impl;

import com.coddicted.buzzma.campaign.entity.Campaign;
import com.coddicted.buzzma.campaign.model.CampaignSummary;
import com.coddicted.buzzma.campaign.service.CampaignService;
import com.coddicted.buzzma.claim.entity.ClaimStatus;
import com.coddicted.buzzma.claim.model.ClaimReviewModel;
import com.coddicted.buzzma.claim.service.ClaimReviewService;
import com.coddicted.buzzma.claim.service.ClaimService;
import com.coddicted.buzzma.identity.entity.BuzzmaUser;
import com.coddicted.buzzma.identity.entity.UserRole;
import com.coddicted.buzzma.shared.common.BaseCrudService;
import java.util.HashSet;
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
import org.springframework.util.CollectionUtils;

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
      final BuzzmaUser requester,
      final Set<UUID> campaignIdsFilter,
      final Set<UUID> mediatorIdsFilter,
      final Set<ClaimStatus> claimStatusFilter,
      final Pageable pageable) {
    // updatedAt-descending ordering is a business rule enforced in the repository queries
    // themselves, so any client-supplied sort is stripped here to avoid a conflicting ORDER BY.
    final Pageable unsortedPageable =
        PageRequest.of(pageable.getPageNumber(), pageable.getPageSize());

    if (requester.getRole() == UserRole.ROLE_MEDIATOR) {
      // A mediator can only ever see their own claims, so mediatorIdsFilter is meaningless here
      // and is ignored entirely.
      return this.claimService.findClaimsToReviewForMediator(
          requester.getId(),
          emptyToNull(campaignIdsFilter),
          emptyToNull(claimStatusFilter),
          unsortedPageable);
    }

    // campaignIdsFilter only ever narrows within the agency's owned campaigns, never broadens it.
    final Set<UUID> campaignIds =
        intersect(getApplicableCampaignIds(requester.getId()), campaignIdsFilter);
    if (campaignIds.isEmpty()) {
      LOGGER.info("No applicable campaigns found for role {}", requester.getRole());
      return Page.empty(unsortedPageable);
    }

    return this.claimService.findClaimsToReviewForCampaigns(
        campaignIds,
        emptyToNull(mediatorIdsFilter),
        emptyToNull(claimStatusFilter),
        unsortedPageable);
  }

  private Set<UUID> getApplicableCampaignIds(final UUID requesterId) {
    return this.campaignService.getByOwnerId(requesterId).stream()
        .map(CampaignSummary::getCampaign)
        .map(Campaign::getId)
        .collect(Collectors.toSet());
  }

  private static Set<UUID> intersect(final Set<UUID> base, final Set<UUID> filter) {
    if (CollectionUtils.isEmpty(filter)) {
      return base;
    }
    final Set<UUID> filterSet = new HashSet<>(filter);
    return base.stream().filter(filterSet::contains).collect(Collectors.toSet());
  }

  private static <T> Set<T> emptyToNull(final Set<T> values) {
    return CollectionUtils.isEmpty(values) ? null : Set.copyOf(values);
  }
}
