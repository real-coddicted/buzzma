package com.coddicted.buzzma.extraction.persistence;

import com.coddicted.buzzma.extraction.entity.ExtractionJob;
import com.coddicted.buzzma.extraction.entity.ExtractionJobStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExtractionJobRepository extends JpaRepository<ExtractionJob, UUID> {

  Optional<ExtractionJob> findByIdAndIsDeletedFalse(UUID id);

  List<ExtractionJob> findByStatusAndAttemptCountLessThan(
      ExtractionJobStatus status, int maxAttempts, Pageable pageable);
}
