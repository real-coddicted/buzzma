package com.coddicted.buzzma.campaign.persistence;

import com.coddicted.buzzma.campaign.entity.CampaignSlot;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CampaignSlotRepository extends JpaRepository<CampaignSlot, UUID> {

  Optional<CampaignSlot> findByCampaignIdAndIsDeletedFalse(UUID campaignId);

  List<CampaignSlot> findByCampaignIdInAndIsDeletedFalse(List<UUID> campaignIds);

  @Modifying
  @Query(
      "UPDATE CampaignSlot cs SET cs.slotsAvailable = cs.slotsAvailable - 1 WHERE cs.id = :id AND cs.slotsAvailable > 0")
  int decrementSlotsAvailableIfPositive(@Param("id") UUID id);
}
