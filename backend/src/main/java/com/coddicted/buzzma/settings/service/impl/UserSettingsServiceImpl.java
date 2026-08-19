package com.coddicted.buzzma.settings.service.impl;

import com.coddicted.buzzma.identity.entity.BuzzmaUser;
import com.coddicted.buzzma.identity.entity.UserRole;
import com.coddicted.buzzma.identity.service.UserService;
import com.coddicted.buzzma.settings.entity.Settings;
import com.coddicted.buzzma.settings.entity.UserSettings;
import com.coddicted.buzzma.settings.entity.UserSettingsFlag;
import com.coddicted.buzzma.settings.persistence.UserSettingsRepository;
import com.coddicted.buzzma.settings.service.UserSettingsService;
import com.coddicted.buzzma.settings.util.SettingsUtils;
import com.coddicted.buzzma.shared.common.BaseCrudService;
import com.coddicted.buzzma.shared.exception.NotFoundException;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserSettingsServiceImpl extends BaseCrudService implements UserSettingsService {

  private static final Logger LOGGER = LoggerFactory.getLogger(UserSettingsServiceImpl.class);

  private final UserSettingsRepository userSettingsRepository;
  private final UserService userService;

  public UserSettingsServiceImpl(
      final UserSettingsRepository userSettingsRepository, final UserService userService) {
    this.userSettingsRepository = userSettingsRepository;
    this.userService = userService;
  }

  @Override
  @Transactional(readOnly = true)
  public UserSettings getByUserId(final UUID userId) {
    return this.userSettingsRepository
        .findByUserIdAndIsDeletedFalse(userId)
        .orElseThrow(() -> new NotFoundException("UserSettings not found for user: " + userId));
  }

  @Override
  @Transactional(readOnly = true)
  public UserSettings getByUserIdOrDefault(final UUID userId) {
    final BuzzmaUser user = this.userService.getById(userId);
    final Settings roleDefaults = getDefaultSettingsByUserRole(user.getRole()).getSettings();
    return this.userSettingsRepository
        .findByUserIdAndIsDeletedFalse(userId)
        .map(
            existing ->
                existing.toBuilder()
                    .settings(mergeWithDefaults(existing.getSettings(), roleDefaults))
                    .build())
        .orElseGet(() -> UserSettings.builder().userId(userId).settings(roleDefaults).build());
  }

  /**
   * A flag left {@code null} on the stored settings means the user has never explicitly overridden
   * it, so it resolves to the current role default rather than a hardcoded false.
   */
  private Settings mergeWithDefaults(final Settings settings, final Settings roleDefaults) {
    Settings.SettingsBuilder builder = Settings.builder();
    for (final UserSettingsFlag flag : UserSettingsFlag.values()) {
      final Boolean override = flag.getValue(settings);
      final Boolean resolved = override != null ? override : flag.getValue(roleDefaults);
      builder = flag.applyTo(builder, resolved);
    }
    return builder.build();
  }

  @Override
  public UserSettings getDefaultSettingsByUserRole(final UserRole role) {
    final Settings settings =
        switch (role) {
          case ROLE_ADMIN -> SettingsUtils.getAdminSettings();
          case ROLE_BRAND -> SettingsUtils.getBrandSettings();
          case ROLE_MEDIATOR -> SettingsUtils.getMediatorSettings();
          case ROLE_AGENCY -> SettingsUtils.getAgencySettings();
          case ROLE_BUYER -> SettingsUtils.getBuyerSettings();
        };
    return UserSettings.builder().settings(settings).build();
  }

  @Override
  @Transactional
  public UserSettings create(final UserSettings userSettings, final UUID requesterId) {
    final UserSettings toSave =
        userSettings.toBuilder().createdBy(requesterId).updatedBy(requesterId).build();
    return this.userSettingsRepository.save(toSave);
  }

  @Override
  @Transactional
  public UserSettings update(final UserSettings userSettings, final UUID requesterId) {
    final UserSettings existing =
        mustFind(this.userSettingsRepository, userSettings.getId(), "UserSettings");
    final UserSettings updated =
        existing.toBuilder().settings(userSettings.getSettings()).updatedBy(requesterId).build();
    return this.userSettingsRepository.save(updated);
  }

  @Override
  @Transactional
  public UserSettings setToDefault(final UUID userId, final UUID requesterId) {
    final BuzzmaUser user = this.userService.getById(userId);
    final Settings defaultSettings = getDefaultSettingsByUserRole(user.getRole()).getSettings();
    return this.userSettingsRepository
        .findByUserIdAndIsDeletedFalse(userId)
        .map(
            existing -> {
              final UserSettings updated =
                  existing.toBuilder().settings(defaultSettings).updatedBy(requesterId).build();
              return this.userSettingsRepository.save(updated);
            })
        .orElseGet(
            () -> {
              final UserSettings toCreate =
                  UserSettings.builder().userId(userId).settings(defaultSettings).build();
              return create(toCreate, requesterId);
            });
  }

  @Override
  @Transactional
  public void delete(final UUID userId, final UUID requesterId) {
    final UserSettings existing =
        this.userSettingsRepository
            .findByUserIdAndIsDeletedFalse(userId)
            .orElseThrow(() -> new NotFoundException("UserSettings not found for user: " + userId));
    LOGGER.warn("Soft-deleting UserSettings for user {}", userId);
    final UserSettings deleted =
        existing.toBuilder().isDeleted(true).updatedBy(requesterId).build();
    this.userSettingsRepository.save(deleted);
  }
}
