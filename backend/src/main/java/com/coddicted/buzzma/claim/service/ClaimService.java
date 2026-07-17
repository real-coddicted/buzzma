package com.coddicted.buzzma.claim.service;

import com.coddicted.buzzma.claim.entity.Claim;
import com.coddicted.buzzma.claim.entity.ClaimScreenshot;
import com.coddicted.buzzma.claim.entity.ClaimStatus;
import com.coddicted.buzzma.claim.entity.ReviewerDecision;
import com.coddicted.buzzma.claim.entity.ScreenshotType;
import com.coddicted.buzzma.claim.entity.ScreenshotVerificationStatus;
import com.coddicted.buzzma.claim.model.ClaimReviewModel;
import com.coddicted.buzzma.claim.model.ClaimWithDeal;
import com.coddicted.buzzma.extraction.entity.ScoredValue;
import com.coddicted.buzzma.identity.entity.UserRole;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ClaimService {

  Claim createClaim(
      Claim claim,
      byte[] screenshot,
      String screenshotFilename,
      String contentType,
      Map<String, ScoredValue> extractedDetails,
      Integer overallScore);

  ClaimWithDeal submitReview(
      UUID claimId,
      UUID ownerId,
      String reviewUrl,
      byte[] screenshot,
      String filename,
      String contentType);

  ClaimWithDeal submitRating(
      UUID claimId, UUID ownerId, byte[] screenshot, String filename, String contentType);

  ClaimWithDeal submitReturn(
      UUID claimId, UUID ownerId, byte[] screenshot, String filename, String contentType);

  Claim getById(UUID claimId, UUID ownerId);

  List<Claim> listByOwner(UUID ownerId);

  List<ClaimScreenshot> listScreenshots(UUID claimId);

  Page<ClaimReviewModel> findClaimsToReviewForMediator(
      UUID mediatorId,
      Collection<UUID> campaignIds,
      Collection<ClaimStatus> claimStatuses,
      Pageable pageable);

  Page<ClaimReviewModel> findClaimsToReviewForCampaigns(
      Collection<UUID> campaignIds,
      Collection<UUID> mediatorIds,
      Collection<ClaimStatus> claimStatuses,
      Pageable pageable);

  ClaimWithDeal reviewScreenshot(
      UUID screenshotId,
      UUID claimId,
      ScreenshotVerificationStatus action,
      UUID reviewerId,
      String reviewerComments);

  ClaimWithDeal updateScreenshot(
      UUID claimId,
      UUID requesterId,
      UUID screenshotId,
      ScreenshotType screenshotType,
      byte[] screenshot,
      String filename,
      String contentType);

  ClaimWithDeal submitClaimReview(
      UUID claimId,
      UUID reviewerId,
      UserRole reviewerRole,
      ReviewerDecision decision,
      String reviewerComment);

  void updateClaimScore(UUID claimId, int score);
}
