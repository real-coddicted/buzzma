package com.coddicted.buzzma.connection.service.impl;

import static com.coddicted.buzzma.connection.service.impl.Fixtures.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import com.coddicted.buzzma.connection.entity.Action;
import com.coddicted.buzzma.connection.entity.Connection;
import com.coddicted.buzzma.connection.entity.ConnectionStatus;
import com.coddicted.buzzma.connection.model.ConnectionSummary;
import com.coddicted.buzzma.connection.model.ConnectionView;
import com.coddicted.buzzma.connection.persistence.ConnectionRepository;
import com.coddicted.buzzma.identity.service.UserService;
import com.coddicted.buzzma.settings.service.UserSettingsService;
import com.coddicted.buzzma.shared.exception.BusinessRuleViolationException;
import com.coddicted.buzzma.shared.exception.NotFoundException;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ConnectionServiceImplTest {

  @Mock private ConnectionRepository mockConnectionRepository;
  @Mock private UserSettingsService mockUserSettingsService;
  @Mock private UserService mockUserService;
  private ConnectionServiceImpl connectionService;

  @BeforeEach
  void setUp() {
    this.connectionService =
        new ConnectionServiceImpl(
            this.mockConnectionRepository, this.mockUserSettingsService, this.mockUserService);
  }

  @Test
  void testGetConnectionsByFromUserIdAndStatus() {
    final Set<ConnectionView> views = Set.of(CONNECTION_VIEW_REQUESTED);
    doReturn(views)
        .when(this.mockConnectionRepository)
        .findViewsByFromUserIdAndStatus(FROM_USER_ID, ConnectionStatus.CONNECTION_STATUS_REQUESTED);

    final Set<ConnectionView> result =
        this.connectionService.getConnectionsByFromUserIdAndStatus(
            FROM_USER_ID, ConnectionStatus.CONNECTION_STATUS_REQUESTED);

    assertEquals(views, result);
  }

  @Test
  void testGetConnectionsByFromUserIdAndStatusWhenStatusNull() {
    final Set<ConnectionView> views = Set.of(CONNECTION_VIEW_REQUESTED, CONNECTION_VIEW_ACCEPTED);
    doReturn(views).when(this.mockConnectionRepository).findViewsByFromUserId(FROM_USER_ID);

    final Set<ConnectionView> result =
        this.connectionService.getConnectionsByFromUserIdAndStatus(FROM_USER_ID, null);

    assertEquals(views, result);
  }

  @Test
  void testGetConnectionSummary() {
    doReturn(CONNECTION_SUMMARY)
        .when(this.mockConnectionRepository)
        .findSummaryByFromUserId(FROM_USER_ID);

    final ConnectionSummary result = this.connectionService.getConnectionSummary(FROM_USER_ID);

    assertEquals(CONNECTION_SUMMARY, result);
  }

  @Test
  void testCreateConnection() {
    doReturn(Optional.empty())
        .when(this.mockConnectionRepository)
        .findByFromUserIdAndToUserIdAndIsDeletedFalse(FROM_USER_ID, TO_USER_ID);

    this.connectionService.createConnection(NEW_CONNECTION);

    final ArgumentCaptor<Connection> captor = ArgumentCaptor.forClass(Connection.class);
    verify(this.mockConnectionRepository).save(captor.capture());
    final Connection saved = captor.getValue();
    assertNull(saved.getId());
    assertEquals(FROM_USER_ID, saved.getFromUserId());
    assertEquals(TO_USER_ID, saved.getToUserId());
    assertEquals(ConnectionStatus.CONNECTION_STATUS_REQUESTED, saved.getStatus());
    assertEquals(FROM_USER_ID, saved.getCreatedBy());
    assertEquals(FROM_USER_ID, saved.getUpdatedBy());
    assertFalse(saved.isDeleted());
  }

  @Test
  void testCreateConnectionWhenAlreadyExists() {
    doReturn(Optional.of(CONNECTION_REQUESTED))
        .when(this.mockConnectionRepository)
        .findByFromUserIdAndToUserIdAndIsDeletedFalse(FROM_USER_ID, TO_USER_ID);

    final BusinessRuleViolationException ex =
        assertThrows(
            BusinessRuleViolationException.class,
            () -> this.connectionService.createConnection(NEW_CONNECTION));
    assertEquals("Connection already exists between these users", ex.getMessage());
  }

  @Test
  void testActionConnectionRequestWhenAccept() {
    doReturn(Optional.of(CONNECTION_REQUESTED))
        .when(this.mockConnectionRepository)
        .findByFromUserIdAndToUserIdAndIsDeletedFalse(FROM_USER_ID, TO_USER_ID);

    final boolean result =
        this.connectionService.actionConnectionRequest(
            FROM_USER_ID, TO_USER_ID, Action.ACTION_ACCEPT, FROM_USER_ID);

    assertTrue(result);
    final ArgumentCaptor<Connection> captor = ArgumentCaptor.forClass(Connection.class);
    verify(this.mockConnectionRepository).save(captor.capture());
    final Connection saved = captor.getValue();
    assertEquals(ConnectionStatus.CONNECTION_STATUS_ACCEPTED, saved.getStatus());
    assertEquals(FROM_USER_ID, saved.getUpdatedBy());
    verify(this.mockUserSettingsService).setToDefault(TO_USER_ID, FROM_USER_ID);
  }

  @Test
  void testActionConnectionRequestWhenReject() {
    doReturn(Optional.of(CONNECTION_REQUESTED))
        .when(this.mockConnectionRepository)
        .findByFromUserIdAndToUserIdAndIsDeletedFalse(FROM_USER_ID, TO_USER_ID);

    final boolean result =
        this.connectionService.actionConnectionRequest(
            FROM_USER_ID, TO_USER_ID, Action.ACTION_REJECT, FROM_USER_ID);

    assertFalse(result);
    final ArgumentCaptor<Connection> captor = ArgumentCaptor.forClass(Connection.class);
    verify(this.mockConnectionRepository).save(captor.capture());
    final Connection saved = captor.getValue();
    assertEquals(ConnectionStatus.CONNECTION_STATUS_REJECTED, saved.getStatus());
    assertEquals(FROM_USER_ID, saved.getUpdatedBy());
  }

  @Test
  void testActionConnectionRequestWhenNotFound() {
    doReturn(Optional.empty())
        .when(this.mockConnectionRepository)
        .findByFromUserIdAndToUserIdAndIsDeletedFalse(FROM_USER_ID, TO_USER_ID);

    final NotFoundException ex =
        assertThrows(
            NotFoundException.class,
            () ->
                this.connectionService.actionConnectionRequest(
                    FROM_USER_ID, TO_USER_ID, Action.ACTION_ACCEPT, FROM_USER_ID));
    assertEquals(
        "Connection not found from " + FROM_USER_ID + " to " + TO_USER_ID, ex.getMessage());
  }

  @Test
  void testActionConnectionRequestWhenNotPending() {
    doReturn(Optional.of(CONNECTION_ACCEPTED))
        .when(this.mockConnectionRepository)
        .findByFromUserIdAndToUserIdAndIsDeletedFalse(FROM_USER_ID, TO_USER_ID);

    final BusinessRuleViolationException ex =
        assertThrows(
            BusinessRuleViolationException.class,
            () ->
                this.connectionService.actionConnectionRequest(
                    FROM_USER_ID, TO_USER_ID, Action.ACTION_ACCEPT, FROM_USER_ID));
    assertEquals("Connection request is no longer pending", ex.getMessage());
  }

  @Test
  void testDeleteWhenFound() {
    doReturn(Optional.of(CONNECTION_REQUESTED))
        .when(this.mockConnectionRepository)
        .findById(CONNECTION_ID);

    this.connectionService.delete(CONNECTION_ID, FROM_USER_ID);

    final ArgumentCaptor<Connection> captor = ArgumentCaptor.forClass(Connection.class);
    verify(this.mockConnectionRepository).save(captor.capture());
    final Connection saved = captor.getValue();
    assertEquals(CONNECTION_ID, saved.getId());
    assertTrue(saved.isDeleted());
    assertEquals(FROM_USER_ID, saved.getUpdatedBy());
  }

  @Test
  void testDeleteWhenNotFound() {
    doReturn(Optional.empty()).when(this.mockConnectionRepository).findById(CONNECTION_ID);

    final NotFoundException ex =
        assertThrows(
            NotFoundException.class,
            () -> this.connectionService.delete(CONNECTION_ID, FROM_USER_ID));
    assertEquals("Connection not found: " + CONNECTION_ID, ex.getMessage());
  }
}
