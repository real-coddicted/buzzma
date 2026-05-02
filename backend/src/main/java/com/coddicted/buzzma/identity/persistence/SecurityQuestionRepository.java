package com.coddicted.buzzma.identity.persistence;

import com.coddicted.buzzma.identity.entity.SecurityQuestion;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SecurityQuestionRepository extends JpaRepository<SecurityQuestion, UUID> {

  List<SecurityQuestion> findAllByIsDeletedFalse();
}
