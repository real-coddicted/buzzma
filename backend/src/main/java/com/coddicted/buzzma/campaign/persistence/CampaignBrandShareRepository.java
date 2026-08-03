package com.coddicted.buzzma.campaign.persistence;

import com.coddicted.buzzma.campaign.entity.CampaignBrandShare;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CampaignBrandShareRepository extends JpaRepository<CampaignBrandShare, UUID> {

  Optional<CampaignBrandShare> findByCampaignId(UUID campaignId);

  List<CampaignBrandShare> findByBrandUserId(UUID brandUserId);

  boolean existsByCampaignId(UUID campaignId);
}
