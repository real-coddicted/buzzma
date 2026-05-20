package com.coddicted.buzzma.campaign.persistence;

import com.coddicted.buzzma.campaign.entity.CampaignSlot;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CampaignSlotRepository extends JpaRepository<CampaignSlot, UUID> {}
