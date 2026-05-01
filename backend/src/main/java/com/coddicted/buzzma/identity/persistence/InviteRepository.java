package com.coddicted.buzzma.identity.persistence;

import com.coddicted.buzzma.identity.entity.Invite;
import com.coddicted.buzzma.identity.entity.UserRole;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InviteRepository extends JpaRepository<Invite, UUID> {

    List<Invite> findAllByIsDeletedFalse();

    Optional<Invite> findByCodeAndIsDeletedFalse(String code);

    Optional<Invite> findByCodeAndRoleAndIsDeletedFalse(String code, UserRole role);
}
