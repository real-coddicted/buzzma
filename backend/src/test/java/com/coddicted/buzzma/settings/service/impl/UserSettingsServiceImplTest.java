package com.coddicted.buzzma.settings.service.impl;

import static com.coddicted.buzzma.settings.service.impl.Fixtures.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.coddicted.buzzma.identity.entity.UserRole;
import com.coddicted.buzzma.identity.service.UserService;
import com.coddicted.buzzma.settings.entity.Settings;
import com.coddicted.buzzma.settings.entity.UserSettings;
import com.coddicted.buzzma.settings.persistence.UserSettingsRepository;
import com.coddicted.buzzma.shared.exception.NotFoundException;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserSettingsServiceImplTest {

  @Mock private UserSettingsRepository mockUserSettingsRepository;
  @Mock private UserService mockUserService;
  private UserSettingsServiceImpl userSettingsService;

  @BeforeEach
  void setUp() {
    this.userSettingsService =
        new UserSettingsServiceImpl(this.mockUserSettingsRepository, this.mockUserService);
  }

  @Test
  void testGetByUserIdWhenFound() {
    when(this.mockUserSettingsRepository.findByUserIdAndIsDeletedFalse(USER_ID))
        .thenReturn(Optional.of(USER_SETTINGS_1));
    // Todo: USER_SETTINGS_1 should be moved to output fixtures folder
    assertEquals(USER_SETTINGS_1, this.userSettingsService.getByUserId(USER_ID));
  }

  @Test
  void testGetByUserIdWhenNotFound() {
    when(this.mockUserSettingsRepository.findByUserIdAndIsDeletedFalse(USER_ID))
        .thenReturn(Optional.empty());

    final NotFoundException ex =
        assertThrows(NotFoundException.class, () -> this.userSettingsService.getByUserId(USER_ID));
    assertEquals("UserSettings not found for user: " + USER_ID, ex.getMessage());
  }

  @Test
  void testGetDefaultSettingsByUserRoleAdmin() {
    final Settings settings =
        this.userSettingsService.getDefaultSettingsByUserRole(UserRole.ROLE_ADMIN).getSettings();

    assertTrue(settings.isDashboardTabEnabled());
    assertTrue(settings.isCampaignsTabEnabled());
    assertTrue(settings.isAssignmentsTabEnabled());
    assertTrue(settings.isConnectionsTabEnabled());
    assertTrue(settings.isDealTabEnabled());
    assertTrue(settings.isTicketsTabEnabled());
    assertTrue(settings.isFeedbackTabEnabled());
    assertTrue(settings.isSettingsTabEnabled());
    assertTrue(settings.isUsersTabEnabled());
  }

  @Test
  void testGetDefaultSettingsByUserRoleMediator() {
    final Settings settings =
        this.userSettingsService.getDefaultSettingsByUserRole(UserRole.ROLE_MEDIATOR).getSettings();

    assertTrue(settings.isDashboardTabEnabled());
    assertFalse(settings.isCampaignsTabEnabled());
    assertTrue(settings.isAssignmentsTabEnabled());
    assertTrue(settings.isConnectionsTabEnabled());
    assertFalse(settings.isDealTabEnabled());
    assertTrue(settings.isTicketsTabEnabled());
    assertTrue(settings.isFeedbackTabEnabled());
    assertTrue(settings.isSettingsTabEnabled());
    assertFalse(settings.isUsersTabEnabled());
  }

  @Test
  void testGetDefaultSettingsByUserRoleBuyer() {
    final Settings settings =
        this.userSettingsService.getDefaultSettingsByUserRole(UserRole.ROLE_BUYER).getSettings();

    assertFalse(settings.isDashboardTabEnabled());
    assertFalse(settings.isCampaignsTabEnabled());
    assertFalse(settings.isAssignmentsTabEnabled());
    assertFalse(settings.isConnectionsTabEnabled());
    assertTrue(settings.isDealTabEnabled());
    assertTrue(settings.isTicketsTabEnabled());
    assertTrue(settings.isFeedbackTabEnabled());
    assertTrue(settings.isSettingsTabEnabled());
    assertFalse(settings.isUsersTabEnabled());
  }

  @Test
  void testCreate() {
    this.userSettingsService.create(USER_SETTINGS_2, REQUESTER_ID);

    final ArgumentCaptor<UserSettings> captor = ArgumentCaptor.forClass(UserSettings.class);
    verify(this.mockUserSettingsRepository).save(captor.capture());
    final UserSettings saved = captor.getValue();
    assertEquals(REQUESTER_ID, saved.getCreatedBy());
    assertEquals(REQUESTER_ID, saved.getUpdatedBy());
    assertEquals(USER_SETTINGS_2.getSettings(), saved.getSettings());
  }

  @Test
  void testUpdateWhenFound() {
    when(this.mockUserSettingsRepository.findById(USER_SETTINGS_ID))
        .thenReturn(Optional.of(USER_SETTINGS_1));

    this.userSettingsService.update(USER_SETTINGS_3, REQUESTER_ID);

    final ArgumentCaptor<UserSettings> captor = ArgumentCaptor.forClass(UserSettings.class);
    verify(this.mockUserSettingsRepository).save(captor.capture());
    final UserSettings saved = captor.getValue();
    assertEquals(USER_SETTINGS_3.getSettings(), saved.getSettings());
    assertEquals(REQUESTER_ID, saved.getUpdatedBy());
  }

  @Test
  void testUpdateWhenNotFound() {
    when(this.mockUserSettingsRepository.findById(USER_SETTINGS_ID)).thenReturn(Optional.empty());

    final NotFoundException ex =
        assertThrows(
            NotFoundException.class,
            () -> this.userSettingsService.update(USER_SETTINGS_3, REQUESTER_ID));
    assertEquals("UserSettings not found: " + USER_SETTINGS_ID, ex.getMessage());
  }

  @Test
  void testDeleteWhenFound() {
    when(this.mockUserSettingsRepository.findByUserIdAndIsDeletedFalse(USER_ID))
        .thenReturn(Optional.of(USER_SETTINGS_1));

    this.userSettingsService.delete(USER_ID, REQUESTER_ID);

    final ArgumentCaptor<UserSettings> captor = ArgumentCaptor.forClass(UserSettings.class);
    verify(this.mockUserSettingsRepository).save(captor.capture());
    final UserSettings saved = captor.getValue();
    assertTrue(saved.getIsDeleted());
    assertEquals(REQUESTER_ID, saved.getUpdatedBy());
  }

  @Test
  void testDeleteWhenNotFound() {
    when(this.mockUserSettingsRepository.findByUserIdAndIsDeletedFalse(USER_ID))
        .thenReturn(Optional.empty());

    final NotFoundException ex =
        assertThrows(
            NotFoundException.class, () -> this.userSettingsService.delete(USER_ID, REQUESTER_ID));
    assertEquals("UserSettings not found for user: " + USER_ID, ex.getMessage());
  }
}
