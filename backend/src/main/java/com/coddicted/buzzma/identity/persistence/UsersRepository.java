package com.coddicted.buzzma.identity.persistence;

import com.coddicted.buzzma.connection.entity.ConnectionStatus;
import com.coddicted.buzzma.identity.entity.BuzzmaUser;
import com.coddicted.buzzma.identity.entity.UserRole;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UsersRepository extends JpaRepository<BuzzmaUser, UUID> {

  Page<BuzzmaUser> findAllByIsDeletedFalse(Pageable pageable);

  Optional<BuzzmaUser> findByMobileAndIsDeletedFalse(String mobile);

  boolean existsUserByMobileAndIsDeletedFalse(String mobile);

  Optional<BuzzmaUser> findFirstByRoleAndIsDeletedFalse(UserRole role);

  /** Of {@code ids}, only those users directly connected (as parent or child) to {@code userId}. */
  @Query(
      """
      SELECT u FROM BuzzmaUser u
      WHERE u.id IN :ids
        AND EXISTS (
          SELECT 1 FROM Connection c
          WHERE c.isDeleted = false
            AND c.status IN :statuses
            AND ((c.fromUserId = :userId AND c.toUserId = u.id)
              OR (c.toUserId = :userId AND c.fromUserId = u.id))
        )
      """)
  List<BuzzmaUser> findByIdInAndConnectedToUser(
      @Param("ids") Collection<UUID> ids,
      @Param("userId") UUID userId,
      @Param("statuses") Collection<ConnectionStatus> statuses);
}
