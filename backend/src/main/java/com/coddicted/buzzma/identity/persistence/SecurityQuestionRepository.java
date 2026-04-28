package com.coddicted.buzzma.identity.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.coddicted.buzzma.identity.entity.SecurityQuestionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SecurityQuestionRepository extends JpaRepository<SecurityQuestionEntity, UUID> {

  List<SecurityQuestionEntity> findAllByUserId(UUID userId);

  Optional<SecurityQuestionEntity> findByUserIdAndQuestionId(UUID userId, Integer questionId);
}
