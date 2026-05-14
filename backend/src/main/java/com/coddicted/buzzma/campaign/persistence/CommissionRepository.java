package com.coddicted.buzzma.campaign.persistence;

import com.coddicted.buzzma.campaign.entity.Commission;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommissionRepository extends JpaRepository<Commission, UUID> {

  Optional<Commission> findByIdAndIsDeletedFalse(UUID id);

  Optional<Commission> findByCampaignIdAndChargedByIdAndIsDeletedFalse(
      UUID campaignId, UUID chargedById);
}