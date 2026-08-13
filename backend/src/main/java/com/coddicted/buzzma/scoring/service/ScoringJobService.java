package com.coddicted.buzzma.scoring.service;

import com.coddicted.buzzma.scoring.entity.ScoringJob;
import java.util.List;
import java.util.UUID;

public interface ScoringJobService {

  ScoringJob processJob(ScoringJob job);

  List<UUID> claimBatchForProcessing(int batchSize, int maxAttempts);
}
