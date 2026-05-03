package com.coddicted.buzzma.identity.persistence;

import com.coddicted.buzzma.identity.entity.SecurityAnswer;
import com.coddicted.buzzma.identity.entity.SecurityQuestionWrapper;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SecurityAnswerRepository extends JpaRepository<SecurityAnswer, UUID> {

  List<SecurityAnswer> findAllByUserId(UUID userId);

  @Query(
      value =
          "SELECT sa.user_id, sa.question_id, sq.question"
              + " FROM security_answers sa"
              + " JOIN security_questions sq ON sq.id = sa.question_id"
              + " WHERE sa.user_id = :userId"
              + " AND sa.is_deleted = false"
              + " AND sq.is_deleted = false",
      nativeQuery = true)
  List<SecurityQuestionWrapper> findSecurityQuestionByUserId(@Param("userId") UUID userId);

  Optional<SecurityAnswer> findByUserIdAndQuestionId(UUID userId, UUID questionId);
}
