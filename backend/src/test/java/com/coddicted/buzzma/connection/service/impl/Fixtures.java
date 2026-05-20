package com.coddicted.buzzma.connection.service.impl;

import com.coddicted.buzzma.connection.entity.Connection;
import com.coddicted.buzzma.shared.util.FileUtils;
import java.util.UUID;

public final class Fixtures {

  static final UUID CONNECTION_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");

  static final UUID FROM_USER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

  static final UUID TO_USER_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");

  static final UUID OTHER_USER_ID = UUID.fromString("55555555-5555-5555-5555-555555555555");

  static final Connection CONNECTION_REQUESTED =
      FileUtils.loadResourceAsObject(
          "/fixtures/input/connection/connection-1.json", Connection.class);

  static final Connection CONNECTION_ACCEPTED =
      FileUtils.loadResourceAsObject(
          "/fixtures/input/connection/connection-2.json", Connection.class);

  static final Connection NEW_CONNECTION =
      FileUtils.loadResourceAsObject(
          "/fixtures/input/connection/connection-3.json", Connection.class);

  private Fixtures() {}
}
