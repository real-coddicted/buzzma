package com.coddicted.buzzma.campaign.persistence;

import com.coddicted.buzzma.campaign.entity.Campaign;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CampaignRepository extends JpaRepository<Campaign, UUID> {

  Optional<Campaign> findByIdAndIsDeletedFalse(UUID id);

  Optional<Campaign> findByIdAndOwnerId(UUID id, UUID ownerId);

  Set<Campaign> findByIdInAndIsDeletedFalse(Set<UUID> ids);

  List<Campaign> findByOwnerIdAndIsDeletedFalse(UUID ownerId);
}
