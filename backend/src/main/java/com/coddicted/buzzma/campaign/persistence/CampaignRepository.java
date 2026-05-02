package com.coddicted.buzzma.campaign.persistence;

import com.coddicted.buzzma.campaign.entity.Campaign;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CampaignRepository extends JpaRepository<Campaign, UUID> {}
