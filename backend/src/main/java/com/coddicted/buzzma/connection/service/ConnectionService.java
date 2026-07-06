package com.coddicted.buzzma.connection.service;

import com.coddicted.buzzma.connection.entity.Action;
import com.coddicted.buzzma.connection.entity.Connection;
import com.coddicted.buzzma.connection.entity.ConnectionStatus;
import com.coddicted.buzzma.connection.model.ConnectionSummary;
import com.coddicted.buzzma.connection.model.ConnectionView;
import java.util.Set;
import java.util.UUID;

public interface ConnectionService {

  Set<ConnectionView> getConnectionsByFromUserIdAndStatus(UUID fromUserId, ConnectionStatus status);

  ConnectionSummary getConnectionSummary(UUID fromUserId);

  Connection createConnection(Connection connection);

  boolean actionConnectionRequest(UUID fromUserId, UUID toUserId, Action action, UUID requesterId);

  void delete(UUID id, UUID requesterId);

  Connection getConnectionByToUserIdAndStatus(final UUID toUserId, final ConnectionStatus status);

  /** True if {@code parentId} invited {@code childId} and the connection is accepted. */
  boolean isParentOf(UUID parentId, UUID childId);
}
