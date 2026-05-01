package com.coddicted.buzzma.campaign.persistence;

import com.coddicted.buzzma.campaign.entity.Commission;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CommissionRepository {
    Commission findByCampaignIdAndChargedById(UUID campaignId, UUID chargedById);
}
