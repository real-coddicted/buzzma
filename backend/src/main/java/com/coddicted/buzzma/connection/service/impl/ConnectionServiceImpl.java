package com.coddicted.buzzma.connection.service.impl;

import com.coddicted.buzzma.connection.entity.Action;
import com.coddicted.buzzma.connection.entity.Connection;
import com.coddicted.buzzma.connection.entity.ConnectionStatus;
import com.coddicted.buzzma.connection.model.ConnectionSummary;
import com.coddicted.buzzma.connection.model.ConnectionView;
import com.coddicted.buzzma.connection.persistence.ConnectionRepository;
import com.coddicted.buzzma.connection.service.ConnectionService;
import com.coddicted.buzzma.identity.entity.BuzzmaUser;
import com.coddicted.buzzma.identity.entity.UserStatus;
import com.coddicted.buzzma.identity.service.UserService;
import com.coddicted.buzzma.settings.service.UserSettingsService;
import com.coddicted.buzzma.shared.common.BaseCrudService;
import com.coddicted.buzzma.shared.exception.BusinessRuleViolationException;
import com.coddicted.buzzma.shared.exception.NotFoundException;
import java.util.Set;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ConnectionServiceImpl extends BaseCrudService implements ConnectionService {

  private static final Logger LOGGER = LoggerFactory.getLogger(ConnectionServiceImpl.class);

  private final ConnectionRepository connectionRepository;
  private final UserSettingsService userSettingsService;
  private final UserService userService;

  public ConnectionServiceImpl(
      final ConnectionRepository connectionRepository,
      final UserSettingsService userSettingsService,
      final UserService userService) {
    this.connectionRepository = connectionRepository;
    this.userSettingsService = userSettingsService;
    this.userService = userService;
  }

  @Override
  @Transactional(readOnly = true)
  public Set<ConnectionView> getConnectionsByFromUserIdAndStatus(
      final UUID fromUserId, final ConnectionStatus status) {
    if (status == null) {
      return this.connectionRepository.findViewsByFromUserId(fromUserId);
    }
    return this.connectionRepository.findViewsByFromUserIdAndStatus(fromUserId, status);
  }

  @Override
  @Transactional(readOnly = true)
  public ConnectionSummary getConnectionSummary(final UUID fromUserId) {
    return this.connectionRepository.findSummaryByFromUserId(fromUserId);
  }

  @Override
  @Transactional
  public Connection createConnection(final Connection connection) {
    final UUID fromUserId = connection.getFromUserId();
    final UUID toUserId = connection.getToUserId();
    if (this.connectionRepository
        .findByFromUserIdAndToUserIdAndIsDeletedFalse(fromUserId, toUserId)
        .isPresent()) {
      LOGGER.warn("Connection already exists from {} to {}", fromUserId, toUserId);
      throw new BusinessRuleViolationException("Connection already exists between these users");
    }
    return this.connectionRepository.save(
        connection.toBuilder()
            .status(ConnectionStatus.CONNECTION_STATUS_REQUESTED)
            .createdBy(fromUserId)
            .updatedBy(fromUserId)
            .isDeleted(false)
            .build());
  }

  @Override
  @Transactional
  public boolean actionConnectionRequest(
      final UUID fromUserId, final UUID toUserId, final Action action, final UUID requesterId) {
    final Connection connection =
        this.connectionRepository
            .findByFromUserIdAndToUserIdAndIsDeletedFalse(fromUserId, toUserId)
            .orElseThrow(
                () ->
                    new NotFoundException(
                        "Connection not found from " + fromUserId + " to " + toUserId));
    validate(connection);
    final ConnectionStatus target = getConnectionStatus(action);
    this.connectionRepository.save(
        connection.toBuilder().status(target).updatedBy(requesterId).build());
    LOGGER.debug("Connection {} transitioned to {} by {}", connection.getId(), target, requesterId);
    if (action == Action.ACTION_ACCEPT) {
      this.userSettingsService.setToDefault(toUserId, requesterId);
    }
    return target == ConnectionStatus.CONNECTION_STATUS_ACCEPTED;
  }

  @Override
  @Transactional
  public void delete(final UUID id, final UUID requesterId) {
    final Connection connection = mustFind(this.connectionRepository, id, "Connection");
    this.connectionRepository.save(
        connection.toBuilder().isDeleted(true).updatedBy(requesterId).build());
  }

  private ConnectionStatus getConnectionStatus(final Action action) {
    return switch (action) {
      case ACTION_ACCEPT -> ConnectionStatus.CONNECTION_STATUS_ACCEPTED;
      case ACTION_REJECT -> ConnectionStatus.CONNECTION_STATUS_REJECTED;
    };
  }

  private void validate(final Connection connection) {
    if (connection.getStatus() != ConnectionStatus.CONNECTION_STATUS_REQUESTED) {
      LOGGER.warn(
          "Connection {} in status {} cannot be actioned",
          connection.getId(),
          connection.getStatus());
      throw new BusinessRuleViolationException("Connection request is no longer pending");
    }
  }

  @Override
  @Transactional(readOnly = true)
  public Connection getConnectionByToUserIdAndStatus(
      final UUID toUserId, final ConnectionStatus status) {
    return this.connectionRepository
        .findByToUserIdAndStatusAndIsDeletedFalse(toUserId, status)
        .orElseThrow(() -> new NotFoundException("Connection not found for " + toUserId));
  }
}
