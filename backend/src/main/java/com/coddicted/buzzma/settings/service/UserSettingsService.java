package com.coddicted.buzzma.settings.service;

import com.coddicted.buzzma.identity.entity.UserRole;
import com.coddicted.buzzma.settings.entity.UserSettings;
import java.util.UUID;

public interface UserSettingsService {
  UserSettings getByUserId(UUID userId);

  UserSettings getDefaultSettingsByUserRole(UserRole role);

  UserSettings create(UserSettings userSettings, UUID requesterId);

  UserSettings update(UserSettings userSettings, UUID requesterId);

  void delete(UUID userId, UUID requesterId);
}
