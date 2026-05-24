package com.coddicted.buzzma.campaign.persistence;

import com.coddicted.buzzma.campaign.entity.CampaignSlot;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CampaignSlotRepository extends JpaRepository<CampaignSlot, UUID> {

  Optional<CampaignSlot> findByCampaignIdAndIsDeletedFalse(UUID campaignId);

  List<CampaignSlot> findByCampaignIdInAndIsDeletedFalse(List<UUID> campaignIds);
}
