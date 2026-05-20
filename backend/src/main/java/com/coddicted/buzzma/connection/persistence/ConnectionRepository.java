package com.coddicted.buzzma.connection.persistence;

import com.coddicted.buzzma.connection.entity.Connection;
import com.coddicted.buzzma.connection.entity.ConnectionStatus;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConnectionRepository extends JpaRepository<Connection, UUID> {

  Optional<Connection> findByFromUserIdAndToUserIdAndIsDeletedFalse(UUID fromUserId, UUID toUserId);

  Set<Connection> findByFromUserIdAndStatusAndIsDeletedFalse(
      UUID fromUserId, ConnectionStatus status);
}
