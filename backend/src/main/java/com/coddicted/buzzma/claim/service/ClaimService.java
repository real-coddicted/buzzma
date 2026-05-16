package com.coddicted.buzzma.claim.service;

import com.coddicted.buzzma.claim.entity.Claim;
import com.coddicted.buzzma.claim.entity.ClaimScreenshot;
import java.util.List;
import java.util.UUID;

public interface ClaimService {

  Claim createClaim(Claim claim, byte[] screenshot, String screenshotFilename, String contentType);

  Claim submitReview(
      UUID claimId,
      UUID ownerId,
      String reviewUrl,
      byte[] screenshot,
      String filename,
      String contentType);

  Claim submitReturn(
      UUID claimId, UUID ownerId, byte[] screenshot, String filename, String contentType);

  Claim getById(UUID claimId, UUID ownerId);

  List<Claim> listByOwner(UUID ownerId);

  List<ClaimScreenshot> listScreenshots(UUID claimId);
}
