package com.coddicted.buzzma.claim.persistence;

import com.coddicted.buzzma.claim.entity.ClaimAccounting;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClaimAccountingRepository extends JpaRepository<ClaimAccounting, UUID> {

  boolean existsByClaimId(UUID claimId);
}
