package com.coddicted.buzzma.invite.persistence;

import com.coddicted.buzzma.invite.entity.Invite;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InviteRepository extends JpaRepository<Invite, UUID> {

  Optional<Invite> findByCodeAndIsDeletedFalse(String inviteCode);

  boolean existsByCode(String inviteCode);
}
