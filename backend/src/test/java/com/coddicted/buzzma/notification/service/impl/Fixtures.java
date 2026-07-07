package com.coddicted.buzzma.notification.service.impl;

import com.coddicted.buzzma.notification.entity.Notification;
import com.coddicted.buzzma.shared.util.FileUtils;
import java.util.UUID;

final class Fixtures {

  static final UUID USER_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");

  static final Notification NOTIFICATION_1 =
      FileUtils.loadResourceAsObject(
          "/fixtures/input/notification/notification-1.json", Notification.class);

  private Fixtures() {}
}
