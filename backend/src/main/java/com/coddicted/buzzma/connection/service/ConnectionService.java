package com.coddicted.buzzma.connection.service;

import com.coddicted.buzzma.connection.entity.Action;
import com.coddicted.buzzma.connection.entity.Connection;
import com.coddicted.buzzma.connection.entity.ConnectionStatus;
import java.util.Set;
import java.util.UUID;

public interface ConnectionService {

  Set<Connection> getConnectionsByFromUserIdAndStatus(UUID fromUserId, ConnectionStatus status);

  Connection createConnection(Connection connection);

  boolean actionConnectionRequest(UUID fromUserId, UUID toUserId, Action action, UUID requesterId);

  void delete(UUID id, UUID requesterId);
}
