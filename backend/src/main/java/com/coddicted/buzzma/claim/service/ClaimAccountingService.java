package com.coddicted.buzzma.claim.service;

import com.coddicted.buzzma.claim.entity.Claim;
import java.util.List;
import java.util.UUID;

public interface ClaimAccountingService {

  List<UUID> claimBatchForProcessing(int batchSize, int maxRetries);

  void processAccounting(Claim claim);
}
