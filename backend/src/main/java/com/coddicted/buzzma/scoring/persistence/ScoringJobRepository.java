package com.coddicted.buzzma.scoring.persistence;

import com.coddicted.buzzma.scoring.entity.ScoringJob;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface ScoringJobRepository extends JpaRepository<ScoringJob, UUID> {

  Optional<ScoringJob> findByIdAndIsDeletedFalse(UUID id);

  @Modifying
  @Transactional
  @Query(
      nativeQuery = true,
      value =
          """
      UPDATE scoring_jobs
      SET status = 'SCORING_JOB_STATUS_PENDING'
      WHERE status = 'SCORING_JOB_STATUS_PROCESSING'
        AND last_attempted_at < NOW() - (INTERVAL '1 minute' * :thresholdMinutes)
      """)
  int resetStaleInProgressJobs(@Param("thresholdMinutes") int thresholdMinutes);
}
