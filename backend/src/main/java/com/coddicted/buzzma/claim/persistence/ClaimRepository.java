package com.coddicted.buzzma.claim.persistence;

import com.coddicted.buzzma.claim.entity.Claim;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClaimRepository extends JpaRepository<Claim, UUID> {

  List<Claim> findByOwnerIdAndIsDeletedFalse(UUID ownerId);

  Optional<Claim> findByIdAndIsDeletedFalse(UUID id);

  boolean existsByOwnerIdAndDealIdAndIsDeletedFalse(UUID ownerId, UUID dealId);
}
