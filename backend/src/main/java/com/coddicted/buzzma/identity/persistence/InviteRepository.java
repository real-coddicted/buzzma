package com.coddicted.buzzma.identity.persistence;

import com.coddicted.buzzma.identity.entity.Invite;
import com.coddicted.buzzma.identity.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface InviteRepository extends JpaRepository<Invite, UUID> {

    Optional<Invite> findByRoleAndCodeAndIsDeletedFalse(UserRole role, String inviteCode);

    boolean existsByRoleAndCodeAndIsDeletedFalse(UserRole role, String inviteCode);

    boolean existsByCode(String inviteCode);
}
