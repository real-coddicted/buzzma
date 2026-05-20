package com.coddicted.buzzma.settings.service.impl;

import com.coddicted.buzzma.settings.entity.UserSettings;
import com.coddicted.buzzma.shared.util.FileUtils;
import java.util.UUID;

public final class Fixtures {

  static final UUID USER_SETTINGS_ID = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");

  static final UUID USER_ID = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");

  static final UUID REQUESTER_ID = UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc");

  static final UserSettings USER_SETTINGS_1 =
      FileUtils.loadResourceAsObject(
          "/fixtures/input/settings/user-settings-1.json", UserSettings.class);

  static final UserSettings USER_SETTINGS_2 =
      FileUtils.loadResourceAsObject(
          "/fixtures/input/settings/user-settings-2.json", UserSettings.class);

  static final UserSettings USER_SETTINGS_3 =
      FileUtils.loadResourceAsObject(
          "/fixtures/input/settings/user-settings-3.json", UserSettings.class);

  private Fixtures() {}
}
