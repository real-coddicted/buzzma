package com.coddicted.buzzma.claim.persistence;

import com.coddicted.buzzma.claim.entity.Claim;
import com.coddicted.buzzma.shared.enums.Platform;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClaimRepository extends JpaRepository<Claim, UUID> {

  List<Claim> findByOwnerIdAndIsDeletedFalse(UUID ownerId);

  Optional<Claim> findByIdAndIsDeletedFalse(UUID id);

  Optional<Claim> findByIdAndOwnerIdAndIsDeletedFalse(UUID id, UUID ownerId);

  boolean existsByEcommerceOrderIdAndPlatformAndIsDeletedFalse(
      String ecommerceOrderId, Platform platform);

  Page<Claim> findByCampaignIdInAndIsDeletedFalse(Collection<UUID> campaignIds, Pageable pageable);
}
