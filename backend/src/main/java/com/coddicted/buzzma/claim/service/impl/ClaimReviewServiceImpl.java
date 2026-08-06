package com.coddicted.buzzma.claim.service.impl;

import com.coddicted.buzzma.campaign.entity.Campaign;
import com.coddicted.buzzma.campaign.entity.CampaignShare;
import com.coddicted.buzzma.campaign.entity.Deal;
import com.coddicted.buzzma.campaign.model.CampaignSummary;
import com.coddicted.buzzma.campaign.service.CampaignService;
import com.coddicted.buzzma.campaign.service.CampaignShareService;
import com.coddicted.buzzma.campaign.service.DealService;
import com.coddicted.buzzma.claim.entity.Claim;
import com.coddicted.buzzma.claim.entity.ClaimReviewStatus;
import com.coddicted.buzzma.claim.entity.ClaimScreenshot;
import com.coddicted.buzzma.claim.entity.ClaimStatus;
import com.coddicted.buzzma.claim.entity.ReviewerDecision;
import com.coddicted.buzzma.claim.entity.ScreenshotVerificationStatus;
import com.coddicted.buzzma.claim.model.ClaimReviewModel;
import com.coddicted.buzzma.claim.model.ClaimWithDeal;
import com.coddicted.buzzma.claim.notification.ClaimReviewEventPublisher;
import com.coddicted.buzzma.claim.service.ClaimReviewService;
import com.coddicted.buzzma.claim.service.ClaimService;
import com.coddicted.buzzma.identity.entity.BuzzmaUser;
import com.coddicted.buzzma.identity.entity.UserRole;
import com.coddicted.buzzma.shared.common.BaseCrudService;
import com.coddicted.buzzma.shared.enums.Platform;
import com.coddicted.buzzma.shared.exception.BusinessRuleViolationException;
import com.coddicted.buzzma.shared.exception.NotFoundException;
import java.math.BigInteger;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
  private final DealService dealService;
  private final ClaimReviewEventPublisher claimReviewEventPublisher;
  private final CampaignShareService campaignShareService;

  public ClaimReviewServiceImpl(
      final ClaimService claimService,
      final CampaignService campaignService,
      final DealService dealService,
      final ClaimReviewEventPublisher claimReviewEventPublisher,
      final CampaignShareService campaignShareService) {
    this.claimService = claimService;
    this.campaignService = campaignService;
    this.dealService = dealService;
    this.claimReviewEventPublisher = claimReviewEventPublisher;
    this.campaignShareService = campaignShareService;
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

  @Override
  @Transactional
  public ClaimWithDeal reviewScreenshot(
      final UUID screenshotId,
      final UUID claimId,
      final ScreenshotVerificationStatus action,
      final UUID reviewerId,
      final String reviewerComments) {

    Claim claim = this.claimService.getById(claimId, reviewerId);
    final Deal deal = this.dealService.getById(claim.getDealId());

    final ClaimScreenshot screenshot = this.claimService.getScreenshotById(screenshotId);

    if (!claimId.equals(screenshot.getClaimId())) {
      throw new NotFoundException("Screenshot not found: " + screenshotId);
    }

    this.claimService.saveScreenshot(
        screenshot.toBuilder()
            .verificationStatus(action)
            .reviewerComments(reviewerComments)
            .updatedAt(Instant.now())
            .updatedBy(reviewerId)
            .build());

    if (action == ScreenshotVerificationStatus.SCREENSHOT_VERIFICATION_STATUS_REJECTED) {
      claim =
          this.claimService.save(
              claim.toBuilder()
                  .status(ClaimStatus.PROOF_REJECTED)
                  .reviewStatus(ClaimReviewStatus.CLAIM_REVIEW_STATUS_OBJECTED)
                  .updatedBy(reviewerId)
                  .build());
      this.claimReviewEventPublisher.publishScreenshotReviewedEvent(
          claim, deal, screenshot.getType(), action, reviewerId, reviewerComments);
    }

    return new ClaimWithDeal(claim, deal);
  }

  @Override
  @Transactional
  public ClaimWithDeal submitClaimReview(
      final UUID claimId,
      final UUID reviewerId,
      final UserRole reviewerRole,
      final ReviewerDecision decision,
      final String reviewerComment,
      final BigInteger amountApprovedPaise) {

    if (reviewerRole == UserRole.ROLE_BRAND) {
      return submitBrandClaimReview(
          claimId, reviewerId, decision, reviewerComment, amountApprovedPaise);
    }

    final Claim claim = this.claimService.getById(claimId, reviewerId);

    if (decision == ReviewerDecision.VERIFIED && reviewerRole != UserRole.ROLE_MEDIATOR) {
      throw new BusinessRuleViolationException(
          "VERIFIED decision is only allowed for MEDIATOR role");
    }
    if (reviewerRole == UserRole.ROLE_MEDIATOR && decision != ReviewerDecision.VERIFIED) {
      throw new BusinessRuleViolationException("MEDIATOR can only submit VERIFIED decision");
    }

    final Claim updated;
    if (reviewerRole == UserRole.ROLE_MEDIATOR) {
      updated =
          this.claimService.save(
              claim.toBuilder().mediatorVerified(true).updatedBy(reviewerId).build());
    } else if (decision == ReviewerDecision.APPROVED) {
      updated =
          approveClaimWithScreenshots(claim, reviewerId, amountApprovedPaise, reviewerComment);
    } else {
      updated =
          this.claimService.save(
              claim.toBuilder()
                  .status(ClaimStatus.REJECTED)
                  .reviewStatus(ClaimReviewStatus.CLAIM_REVIEW_STATUS_REJECTED)
                  .reviewerComments(reviewerComment)
                  .reviewerId(reviewerId)
                  .updatedAt(Instant.now())
                  .updatedBy(reviewerId)
                  .build());
    }

    return new ClaimWithDeal(updated, this.dealService.getById(updated.getDealId()));
  }

  @Override
  @Transactional
  public List<ClaimWithDeal> bulkApproveClaimReviews(
      final Map<UUID, BigInteger> claimAmounts, final UUID reviewerId) {
    final List<ClaimWithDeal> results = new ArrayList<>();
    for (final Map.Entry<UUID, BigInteger> entry : claimAmounts.entrySet()) {
      final Claim claim = this.claimService.getById(entry.getKey(), reviewerId);
      final Claim updated = approveClaimWithScreenshots(claim, reviewerId, entry.getValue(), null);
      results.add(new ClaimWithDeal(updated, this.dealService.getById(updated.getDealId())));
    }
    return results;
  }

  private Claim approveClaimWithScreenshots(
      final Claim claim,
      final UUID reviewerId,
      final BigInteger amountApprovedPaise,
      final String reviewerComments) {
    this.claimService
        .listScreenshots(claim.getId())
        .forEach(
            s ->
                this.claimService.saveScreenshot(
                    s.toBuilder()
                        .verificationStatus(
                            ScreenshotVerificationStatus.SCREENSHOT_VERIFICATION_STATUS_VERIFIED)
                        .updatedAt(Instant.now())
                        .updatedBy(reviewerId)
                        .build()));
    // If the campaign has been shared with a brand, the agency's approval isn't final — it routes
    // to the brand for their own sign-off before the claim is truly APPROVED.
    final boolean campaignSharedWithBrand =
        this.campaignShareService.existsByCampaignId(claim.getCampaignId());
    return this.claimService.save(
        claim.toBuilder()
            .status(campaignSharedWithBrand ? ClaimStatus.UNDER_BRAND_REVIEW : ClaimStatus.APPROVED)
            .reviewStatus(ClaimReviewStatus.CLAIM_REVIEW_STATUS_APPROVED)
            .reviewerComments(reviewerComments)
            .reviewerId(reviewerId)
            .amountApprovedPaise(amountApprovedPaise)
            .updatedAt(Instant.now())
            .updatedBy(reviewerId)
            .build());
  }

  private ClaimWithDeal submitBrandClaimReview(
      final UUID claimId,
      final UUID brandUserId,
      final ReviewerDecision decision,
      final String reviewerComment,
      final BigInteger amountApprovedPaise) {
    if (decision == ReviewerDecision.VERIFIED) {
      throw new BusinessRuleViolationException("VERIFIED decision is not allowed for brand review");
    }
    final Claim claim = loadAndVerifyBrandAccess(claimId, brandUserId);
    final Claim updated =
        decision == ReviewerDecision.APPROVED
            ? approveBrandClaim(claim, brandUserId, amountApprovedPaise, reviewerComment)
            : rejectBrandClaim(claim, brandUserId, reviewerComment);
    return new ClaimWithDeal(updated, this.dealService.getById(updated.getDealId()));
  }

  private Claim approveBrandClaim(
      final Claim claim,
      final UUID brandUserId,
      final BigInteger amountApprovedPaise,
      final String reviewerComment) {
    if (amountApprovedPaise == null) {
      throw new BusinessRuleViolationException("Approved amount is required");
    }
    return this.claimService.save(
        claim.toBuilder()
            .status(ClaimStatus.APPROVED)
            .brandReviewStatus(ReviewerDecision.APPROVED)
            .brandReviewerId(brandUserId)
            .amountApprovedPaise(amountApprovedPaise)
            .reviewerComments(reviewerComment)
            .updatedAt(Instant.now())
            .updatedBy(brandUserId)
            .build());
  }

  private Claim rejectBrandClaim(
      final Claim claim, final UUID brandUserId, final String reviewerComment) {
    return this.claimService.save(
        claim.toBuilder()
            .status(ClaimStatus.REJECTED)
            .brandReviewStatus(ReviewerDecision.REJECTED)
            .brandReviewerId(brandUserId)
            .reviewerComments(reviewerComment)
            .updatedAt(Instant.now())
            .updatedBy(brandUserId)
            .build());
  }

  private Claim loadAndVerifyBrandAccess(final UUID claimId, final UUID brandUserId) {
    // claimService.getById already grants view access to the brand a campaign was shared with
    // (via CampaignShareService), so this only needs to additionally confirm the claim is
    // actually pending that brand's review.
    final Claim claim = this.claimService.getById(claimId, brandUserId);
    if (claim.getStatus() != ClaimStatus.UNDER_BRAND_REVIEW) {
      LOGGER.warn(
          "Brand {} is not authorized to review claim {} (status: {})",
          brandUserId,
          claimId,
          claim.getStatus());
      throw new NotFoundException("Claim not found: " + claimId);
    }
    return claim;
  }

  @Override
  @Transactional(readOnly = true)
  public List<ClaimReviewModel> findClaimReviewModels(final Collection<UUID> claimIds) {
    return this.claimService.findClaimReviewModels(claimIds);
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
        this.campaignShareService.findByToUserId(requester.getId()).stream()
            .map(CampaignShare::getCampaignId)
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
