package com.coddicted.buzzma.identity.service.impl;

import com.coddicted.buzzma.identity.entity.BuzzmaUser;
import com.coddicted.buzzma.util.FileUtils;
import java.util.UUID;

public final class Fixtures {

  static final UUID USER_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");

  static final UUID REQUESTER_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");

  static final String MOBILE = "9876543210";

  static final BuzzmaUser USER_1 =
      FileUtils.loadResourceAsObject(
          "/fixtures/input/identity/buzzma-user-1.json", BuzzmaUser.class);

  static final BuzzmaUser EXPECTED_USER_1 =
      FileUtils.loadResourceAsObject(
          "/fixtures/output/identity/buzzma-user-1.json", BuzzmaUser.class);

  static final BuzzmaUser USER_2 =
      FileUtils.loadResourceAsObject(
          "/fixtures/input/identity/buzzma-user-2.json", BuzzmaUser.class);

  static final BuzzmaUser EXPECTED_USER_2 =
      FileUtils.loadResourceAsObject(
          "/fixtures/output/identity/buzzma-user-2.json", BuzzmaUser.class);

  private Fixtures() {}
}
