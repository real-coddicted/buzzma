package com.coddicted.buzzma.claim.service;

import com.coddicted.buzzma.claim.entity.ClaimStatus;
import com.coddicted.buzzma.claim.entity.ReviewerDecision;
import com.coddicted.buzzma.claim.entity.ScreenshotVerificationStatus;
import com.coddicted.buzzma.claim.model.ClaimReviewModel;
import com.coddicted.buzzma.claim.model.ClaimWithDeal;
import com.coddicted.buzzma.identity.entity.BuzzmaUser;
import com.coddicted.buzzma.identity.entity.UserRole;
import com.coddicted.buzzma.shared.enums.Platform;
import java.math.BigInteger;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ClaimReviewService {

  Page<ClaimReviewModel> getClaimReviews(
      BuzzmaUser requester,
      Set<UUID> campaignIdsFilter,
      Set<UUID> mediatorIdsFilter,
      Set<ClaimStatus> claimStatusFilter,
      Set<String> brandsFilter,
      Set<Platform> platformsFilter,
      Pageable pageable);

  ClaimWithDeal reviewScreenshot(
      UUID screenshotId,
      UUID claimId,
      ScreenshotVerificationStatus action,
      UUID reviewerId,
      String reviewerComments);

  ClaimWithDeal submitClaimReview(
      UUID claimId,
      UUID reviewerId,
      UserRole reviewerRole,
      ReviewerDecision decision,
      String reviewerComment,
      BigInteger amountApprovedPaise);

  List<ClaimWithDeal> bulkApproveClaimReviews(Map<UUID, BigInteger> claimAmounts, UUID reviewerId);

  List<ClaimWithDeal> bulkBrandVerifyClaimReviews(Collection<UUID> claimIds, UUID reviewerId);

  List<ClaimReviewModel> findClaimReviewModels(Collection<UUID> claimIds);
}
