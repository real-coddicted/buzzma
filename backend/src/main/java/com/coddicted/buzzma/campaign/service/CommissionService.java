package com.coddicted.buzzma.campaign.service;

import com.coddicted.buzzma.campaign.entity.Commission;
import java.util.UUID;

public interface CommissionService {
  Commission getCommissionCharged(UUID campaignId, UUID chargedById);

  Commission create(Commission commission, UUID requesterId);

  Commission update(Commission commission, UUID requesterId);

  Commission delete(UUID commissionId, UUID requesterId);
}
