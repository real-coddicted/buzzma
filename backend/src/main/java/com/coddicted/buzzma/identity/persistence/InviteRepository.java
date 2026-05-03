package com.coddicted.buzzma.identity.persistence;

import com.coddicted.buzzma.identity.entity.Invite;
import com.coddicted.buzzma.identity.entity.UserRole;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InviteRepository extends JpaRepository<Invite, UUID> {

  Optional<Invite> findByRoleAndCodeAndIsDeletedFalse(UserRole role, String inviteCode);

  boolean existsByCode(String code);
}
