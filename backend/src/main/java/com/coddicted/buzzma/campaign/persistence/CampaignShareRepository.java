package com.coddicted.buzzma.campaign.persistence;

import com.coddicted.buzzma.campaign.entity.CampaignShare;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CampaignShareRepository extends JpaRepository<CampaignShare, UUID> {

  Optional<CampaignShare> findByCampaignId(UUID campaignId);

  List<CampaignShare> findByToUserId(UUID toUserId);

  boolean existsByCampaignId(UUID campaignId);
}
