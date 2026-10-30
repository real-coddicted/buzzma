package com.coddicted.buzzma.identity.persistence;

import com.coddicted.buzzma.identity.entity.BuzzmaUser;
import com.coddicted.buzzma.shared.util.FileUtils;

public final class Fixtures {

  static final String SEARCH_TERM = "ravi";

  static final BuzzmaUser RAVI_BUYER =
      FileUtils.loadResourceAsObject(
          "/fixtures/input/identity/search-ravi-buyer.json", BuzzmaUser.class);

  static final BuzzmaUser RAVI_MEDIATOR =
      FileUtils.loadResourceAsObject(
          "/fixtures/input/identity/search-ravi-mediator.json", BuzzmaUser.class);

  static final BuzzmaUser RAVI_AGENCY =
      FileUtils.loadResourceAsObject(
          "/fixtures/input/identity/search-ravi-agency.json", BuzzmaUser.class);

  static final BuzzmaUser RAVI_DELETED_BUYER =
      FileUtils.loadResourceAsObject(
          "/fixtures/input/identity/search-ravi-deleted-buyer.json", BuzzmaUser.class);

  private Fixtures() {}
}
