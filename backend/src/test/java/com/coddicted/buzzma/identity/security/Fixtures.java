package com.coddicted.buzzma.identity.security;

import com.coddicted.buzzma.identity.entity.BuzzmaUser;
import com.coddicted.buzzma.shared.util.FileUtils;

public final class Fixtures {

  static final BuzzmaUser ADMIN =
      FileUtils.loadResourceAsObject(
          "/fixtures/input/identity/view-as-admin.json", BuzzmaUser.class);

  static final BuzzmaUser OTHER_ADMIN =
      FileUtils.loadResourceAsObject(
          "/fixtures/input/identity/view-as-other-admin.json", BuzzmaUser.class);

  static final BuzzmaUser BUYER =
      FileUtils.loadResourceAsObject(
          "/fixtures/input/identity/view-as-buyer.json", BuzzmaUser.class);

  static final BuzzmaUser DELETED_BUYER =
      FileUtils.loadResourceAsObject(
          "/fixtures/input/identity/view-as-deleted-buyer.json", BuzzmaUser.class);

  static final BuzzmaUser AGENCY =
      FileUtils.loadResourceAsObject(
          "/fixtures/input/identity/view-as-agency.json", BuzzmaUser.class);

  static final BuzzmaUser BRAND =
      FileUtils.loadResourceAsObject(
          "/fixtures/input/identity/view-as-brand.json", BuzzmaUser.class);

  private Fixtures() {}
}
