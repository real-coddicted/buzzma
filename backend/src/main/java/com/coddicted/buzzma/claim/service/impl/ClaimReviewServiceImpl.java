package com.coddicted.buzzma.claim.service.impl;

import com.coddicted.buzzma.campaign.entity.Campaign;
import com.coddicted.buzzma.campaign.entity.CampaignBrandShare;
import com.coddicted.buzzma.campaign.model.CampaignSummary;
import com.coddicted.buzzma.campaign.service.CampaignBrandShareService;
import com.coddicted.buzzma.campaign.service.CampaignService;
import com.coddicted.buzzma.claim.entity.ClaimReviewStatus;
import com.coddicted.buzzma.claim.entity.ClaimStatus;
import com.coddicted.buzzma.claim.model.ClaimReviewModel;
import com.coddicted.buzzma.claim.service.ClaimReviewService;
import com.coddicted.buzzma.claim.service.ClaimService;
import com.coddicted.buzzma.identity.entity.BuzzmaUser;
import com.coddicted.buzzma.identity.entity.UserRole;
import com.coddicted.buzzma.shared.common.BaseCrudService;
import com.coddicted.buzzma.shared.enums.Platform;
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
  private final CampaignBrandShareService campaignBrandShareService;

  public ClaimReviewServiceImpl(
      final ClaimService claimService,
      final CampaignService campaignService,
      final CampaignBrandShareService campaignBrandShareService) {
    this.claimService = claimService;
    this.campaignService = campaignService;
    this.campaignBrandShareService = campaignBrandShareService;
  }

  @Override
  @Transactional(readOnly = true)
  public Page<ClaimReviewModel> getClaimReviews(
      final BuzzmaUser requester,
      final Set<UUID> campaignIdsFilter,
      final Set<UUID> mediatorIdsFilter,
      final Set<ClaimStatus> claimStatusFilter,
      final Set<String> brandsFilter,
      final Set<Platform> platformsFilter,
      final Set<ClaimReviewStatus> reviewStatusesFilter,
      final Pageable pageable) {
    // updatedAt-descending ordering is a business rule enforced in the repository queries
    // themselves, so any client-supplied sort is stripped here to avoid a conflicting ORDER BY.
    // Pageable.unpaged() (used by report generation to fetch every matching row) throws on
    // getPageNumber()/getPageSize(), so it must be passed through as-is rather than rebuilt.
    final Pageable unsortedPageable =
        pageable.isPaged()
            ? PageRequest.of(pageable.getPageNumber(), pageable.getPageSize())
            : Pageable.unpaged();

    if (requester.getRole() == UserRole.ROLE_MEDIATOR) {
      // A mediator can only ever see their own claims, so mediatorIdsFilter is meaningless here
      // and is ignored entirely.
      return this.claimService.findClaimsToReviewForMediator(
          requester.getId(),
          emptyToNull(campaignIdsFilter),
          emptyToNull(claimStatusFilter),
          emptyToNull(brandsFilter),
          emptyToNull(platformsFilter),
          emptyToNull(reviewStatusesFilter),
          unsortedPageable);
    }

    // campaignIdsFilter only ever narrows within the agency's owned campaigns, never broadens it.
    final Set<UUID> campaignIds = intersect(getApplicableCampaignIds(requester), campaignIdsFilter);
    if (campaignIds.isEmpty()) {
      LOGGER.info("No applicable campaigns found for role {}", requester.getRole());
      return Page.empty(unsortedPageable);
    }

    return this.claimService.findClaimsToReviewForCampaigns(
        campaignIds,
        emptyToNull(mediatorIdsFilter),
        emptyToNull(claimStatusFilter),
        emptyToNull(brandsFilter),
        emptyToNull(platformsFilter),
        emptyToNull(reviewStatusesFilter),
        unsortedPageable);
  }

  private Set<UUID> getApplicableCampaignIds(final BuzzmaUser requester) {
    final Set<UUID> ownedCampaignIds =
        this.campaignService.getByOwnerId(requester.getId()).stream()
            .map(CampaignSummary::getCampaign)
            .map(Campaign::getId)
            .collect(Collectors.toSet());
    if (requester.getRole() != UserRole.ROLE_BRAND) {
      return ownedCampaignIds;
    }
    // A brand also sees claims for campaigns an agency has shared with them, in addition to any
    // campaigns they own directly.
    final Set<UUID> sharedCampaignIds =
        this.campaignBrandShareService.findByBrandUserId(requester.getId()).stream()
            .map(CampaignBrandShare::getCampaignId)
            .collect(Collectors.toSet());
    final Set<UUID> applicable = new HashSet<>(ownedCampaignIds);
    applicable.addAll(sharedCampaignIds);
    return applicable;
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
