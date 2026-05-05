package com.coddicted.buzzma.identity.service.impl;

import com.coddicted.buzzma.identity.entity.BuzzmaUser;
import com.coddicted.buzzma.identity.entity.UserBankingDetail;
import com.coddicted.buzzma.identity.entity.UserCredential;
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

  static final String PLAIN_PASSWORD = "plain-password";

  static final String STORED_HASH = "stored-hash";

  static final String NEW_HASH = "new-hashed-password";

  static final UserCredential USER_CREDENTIAL_1 =
      FileUtils.loadResourceAsObject(
          "/fixtures/input/identity/user-credential-1.json", UserCredential.class);

  static final UserCredential USER_CREDENTIAL_2 =
      FileUtils.loadResourceAsObject(
          "/fixtures/input/identity/user-credential-2.json", UserCredential.class);

  static final UserBankingDetail BANKING_DETAIL_1 =
      FileUtils.loadResourceAsObject(
          "/fixtures/input/identity/user-banking-detail-1.json", UserBankingDetail.class);

  private Fixtures() {}
}
