package com.coddicted.buzzma.identity.persistence;

import com.coddicted.buzzma.identity.entity.SecurityAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SecurityAnswerRepository extends JpaRepository<SecurityAnswer, UUID> {

    List<SecurityAnswer> findAllByUserId(UUID userId);

    Optional<SecurityAnswer> findByUserIdAndQuestionId(UUID userId, UUID questionId);
}
