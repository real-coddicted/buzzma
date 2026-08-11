package com.coddicted.buzzma.terms.persistence;

import com.coddicted.buzzma.terms.entity.UserTermsAcceptance;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserTermsAcceptanceRepository extends JpaRepository<UserTermsAcceptance, UUID> {
  Optional<UserTermsAcceptance> findTopByUserIdOrderByAcceptedAtDesc(UUID userId);
}
