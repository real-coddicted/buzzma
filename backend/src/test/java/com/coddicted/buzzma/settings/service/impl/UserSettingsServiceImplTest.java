package com.coddicted.buzzma.settings.service.impl;

import static com.coddicted.buzzma.settings.service.impl.Fixtures.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.coddicted.buzzma.identity.entity.BuzzmaUser;
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
  void testGetByUserIdOrDefaultWhenFound() {
    when(this.mockUserSettingsRepository.findByUserIdAndIsDeletedFalse(USER_ID))
        .thenReturn(Optional.of(USER_SETTINGS_1));
    when(this.mockUserService.getById(USER_ID))
        .thenReturn(BuzzmaUser.builder().id(USER_ID).role(UserRole.ROLE_BUYER).build());

    // USER_SETTINGS_1's fixture never sets myPaymentsTabEnabled/userPayoutsTabEnabled/
    // myClaimsTabEnabled (null on the stored settings), so they resolve from the buyer role
    // default (false, false, true respectively) rather than staying null.
    final Settings stored = USER_SETTINGS_1.getSettings();
    final Settings expected =
        Settings.builder()
            .dashboardTabEnabled(stored.getDashboardTabEnabled())
            .campaignsTabEnabled(stored.getCampaignsTabEnabled())
            .assignmentsTabEnabled(stored.getAssignmentsTabEnabled())
            .connectionsTabEnabled(stored.getConnectionsTabEnabled())
            .dealTabEnabled(stored.getDealTabEnabled())
            .myClaimsTabEnabled(true)
            .claimReviewEnabled(stored.getClaimReviewEnabled())
            .ticketsTabEnabled(stored.getTicketsTabEnabled())
            .feedbackTabEnabled(stored.getFeedbackTabEnabled())
            .settingsTabEnabled(stored.getSettingsTabEnabled())
            .usersTabEnabled(stored.getUsersTabEnabled())
            .myPaymentsTabEnabled(false)
            .userPayoutsTabEnabled(false)
            .build();

    assertEquals(expected, this.userSettingsService.getByUserIdOrDefault(USER_ID).getSettings());
  }

  @Test
  void testGetByUserIdOrDefaultWhenFoundFillsUnsetFlagsFromRoleDefaults() {
    when(this.mockUserSettingsRepository.findByUserIdAndIsDeletedFalse(USER_ID))
        .thenReturn(Optional.of(USER_SETTINGS_2));
    when(this.mockUserService.getById(USER_ID))
        .thenReturn(BuzzmaUser.builder().id(USER_ID).role(UserRole.ROLE_BUYER).build());

    final Settings settings = this.userSettingsService.getByUserIdOrDefault(USER_ID).getSettings();

    // USER_SETTINGS_2's fixture never sets myPaymentsTabEnabled/userPayoutsTabEnabled/
    // myClaimsTabEnabled, so they should resolve from the buyer role default rather than
    // staying null.
    assertEquals(
        this.userSettingsService
            .getDefaultSettingsByUserRole(UserRole.ROLE_BUYER)
            .getSettings()
            .getMyPaymentsTabEnabled(),
        settings.getMyPaymentsTabEnabled());
    assertNotNull(settings.getUserPayoutsTabEnabled());
    assertEquals(
        this.userSettingsService
            .getDefaultSettingsByUserRole(UserRole.ROLE_BUYER)
            .getSettings()
            .getMyClaimsTabEnabled(),
        settings.getMyClaimsTabEnabled());
  }

  @Test
  void testGetByUserIdOrDefaultWhenNotFoundFallsBackToRoleDefaults() {
    when(this.mockUserSettingsRepository.findByUserIdAndIsDeletedFalse(USER_ID))
        .thenReturn(Optional.empty());
    when(this.mockUserService.getById(USER_ID))
        .thenReturn(BuzzmaUser.builder().id(USER_ID).role(UserRole.ROLE_BUYER).build());

    final Settings settings = this.userSettingsService.getByUserIdOrDefault(USER_ID).getSettings();

    assertEquals(
        this.userSettingsService.getDefaultSettingsByUserRole(UserRole.ROLE_BUYER).getSettings(),
        settings);
  }

  @Test
  void testGetDefaultSettingsByUserRoleAdmin() {
    final Settings settings =
        this.userSettingsService.getDefaultSettingsByUserRole(UserRole.ROLE_ADMIN).getSettings();

    assertFalse(settings.getDashboardTabEnabled());
    assertTrue(settings.getCampaignsTabEnabled());
    assertTrue(settings.getAssignmentsTabEnabled());
    assertTrue(settings.getConnectionsTabEnabled());
    assertTrue(settings.getDealTabEnabled());
    assertFalse(settings.getMyClaimsTabEnabled());
    assertTrue(settings.getTicketsTabEnabled());
    assertTrue(settings.getFeedbackTabEnabled());
    assertTrue(settings.getSettingsTabEnabled());
    assertTrue(settings.getUsersTabEnabled());
  }

  @Test
  void testGetDefaultSettingsByUserRoleMediator() {
    final Settings settings =
        this.userSettingsService.getDefaultSettingsByUserRole(UserRole.ROLE_MEDIATOR).getSettings();

    assertFalse(settings.getDashboardTabEnabled());
    assertFalse(settings.getCampaignsTabEnabled());
    assertTrue(settings.getAssignmentsTabEnabled());
    assertTrue(settings.getConnectionsTabEnabled());
    assertFalse(settings.getDealTabEnabled());
    assertFalse(settings.getMyClaimsTabEnabled());
    assertTrue(settings.getTicketsTabEnabled());
    assertTrue(settings.getFeedbackTabEnabled());
    assertTrue(settings.getSettingsTabEnabled());
    assertFalse(settings.getUsersTabEnabled());
  }

  @Test
  void testGetDefaultSettingsByUserRoleBuyer() {
    final Settings settings =
        this.userSettingsService.getDefaultSettingsByUserRole(UserRole.ROLE_BUYER).getSettings();

    assertFalse(settings.getDashboardTabEnabled());
    assertFalse(settings.getCampaignsTabEnabled());
    assertFalse(settings.getAssignmentsTabEnabled());
    assertTrue(settings.getConnectionsTabEnabled());
    assertTrue(settings.getDealTabEnabled());
    assertTrue(settings.getMyClaimsTabEnabled());
    assertTrue(settings.getTicketsTabEnabled());
    assertTrue(settings.getFeedbackTabEnabled());
    assertTrue(settings.getSettingsTabEnabled());
    assertFalse(settings.getUsersTabEnabled());
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
