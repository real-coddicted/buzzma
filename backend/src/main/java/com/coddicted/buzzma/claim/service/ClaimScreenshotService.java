package com.coddicted.buzzma.claim.service;

import com.coddicted.buzzma.campaign.entity.CampaignStepType;
import com.coddicted.buzzma.extraction.entity.ExtractionJob;
import com.coddicted.buzzma.extraction.entity.ExtractionResult;
import com.coddicted.buzzma.scoring.entity.ScoringJob;
import java.util.UUID;

public interface ClaimScreenshotService {

  ExtractionResult extractSync(
      CampaignStepType stepType,
      byte[] imageBytes,
      String originalFilename,
      String contentType,
      UUID requesterId,
      UUID campaignId);

  /** Returns true if the extracted screenshot's step requires scoring. */
  boolean process(ExtractionJob job);

  void processScoring(ScoringJob job);
}
