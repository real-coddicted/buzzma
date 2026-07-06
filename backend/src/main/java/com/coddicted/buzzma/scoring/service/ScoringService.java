package com.coddicted.buzzma.scoring.service;

import com.coddicted.buzzma.scoring.entity.ScoringJob;
import java.util.UUID;

public interface ScoringService {

  ScoringJob submitJob(UUID claimScreenshotId, UUID requesterId);

  ScoringJob getById(UUID jobId);
}
