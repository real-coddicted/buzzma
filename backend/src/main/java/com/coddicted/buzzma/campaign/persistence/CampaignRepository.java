package com.coddicted.buzzma.campaign.persistence;

import java.util.UUID;

import com.coddicted.buzzma.campaign.entity.Campaign;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CampaignRepository extends JpaRepository<Campaign, UUID> {

  Page<Campaign> findAllByOwnerIdAndOwnerTypeAndIsDeletedFalse(UUID ownerId, OwnerType ownerType, Pageable pageable);
}
