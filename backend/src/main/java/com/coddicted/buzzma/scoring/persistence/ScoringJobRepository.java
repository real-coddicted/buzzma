package com.coddicted.buzzma.scoring.persistence;

import com.coddicted.buzzma.scoring.entity.ScoringJob;
import com.coddicted.buzzma.scoring.entity.ScoringJobStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ScoringJobRepository extends JpaRepository<ScoringJob, UUID> {

  Optional<ScoringJob> findByIdAndIsDeletedFalse(UUID id);

  List<ScoringJob> findByStatusAndAttemptCountLessThan(
      ScoringJobStatus status, int maxAttempts, Pageable pageable);
}
