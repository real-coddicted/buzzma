package com.coddicted.buzzma.campaign.persistence;

import com.coddicted.buzzma.campaign.entity.Commission;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public interface CommissionRepository {
  Commission findByCampaignIdAndChargedById(UUID campaignId, UUID chargedById);
}
