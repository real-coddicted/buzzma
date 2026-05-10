package com.coddicted.buzzma.feedback.persistence;

import com.coddicted.buzzma.feedback.entity.Feedback;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, UUID> {

  Optional<Feedback> findByIdAndIsDeletedFalse(UUID id);

  List<Feedback> findByUserIdAndIsDeletedFalse(UUID userId);
}
