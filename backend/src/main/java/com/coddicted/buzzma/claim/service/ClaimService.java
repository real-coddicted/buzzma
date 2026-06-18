package com.coddicted.buzzma.claim.service;

import com.coddicted.buzzma.claim.entity.Claim;
import com.coddicted.buzzma.claim.entity.ClaimScreenshot;
import com.coddicted.buzzma.claim.entity.ScreenshotVerificationStatus;
import com.coddicted.buzzma.claim.model.ClaimWithDeal;
import com.coddicted.buzzma.extraction.entity.ScoredValue;
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
      Double overallScore);

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

  Page<Claim> listClaimByCampaignIds(List<UUID> campaignIdList, Pageable pageable);

  ClaimWithDeal reviewScreenshot(
      UUID screenshotId, UUID claimId, ScreenshotVerificationStatus action, UUID reviewerId);
}
