package com.coddicted.buzzma.identity.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.coddicted.buzzma.identity.entity.SecurityQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SecurityQuestionRepository extends JpaRepository<SecurityQuestion, UUID> {

  List<SecurityQuestion> findAllByIsDeletedFalse();
}
