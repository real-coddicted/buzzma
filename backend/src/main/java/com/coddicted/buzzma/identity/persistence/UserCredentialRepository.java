package com.coddicted.buzzma.identity.persistence;

import com.coddicted.buzzma.identity.entity.UserCredential;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserCredentialRepository extends JpaRepository<UserCredential, UUID> {

  Optional<UserCredential> findByUserIdAndIsDeletedFalse(UUID userId);
}
