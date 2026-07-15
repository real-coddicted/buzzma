package com.coddicted.buzzma.connection.service.impl;

import com.coddicted.buzzma.connection.entity.Connection;
import com.coddicted.buzzma.connection.model.ConnectionSummary;
import com.coddicted.buzzma.connection.model.ConnectionView;
import com.coddicted.buzzma.identity.entity.BuzzmaUser;
import com.coddicted.buzzma.identity.entity.UserRole;
import com.coddicted.buzzma.invite.entity.Invite;
import com.coddicted.buzzma.invite.entity.InviteStatus;
import com.coddicted.buzzma.shared.util.FileUtils;
import java.util.UUID;

public final class Fixtures {

  static final UUID CONNECTION_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");

  static final UUID FROM_USER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

  static final UUID TO_USER_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");

  static final Connection CONNECTION_REQUESTED =
      FileUtils.loadResourceAsObject(
          "/fixtures/input/connection/connection-1.json", Connection.class);

  static final Connection CONNECTION_ACCEPTED =
      FileUtils.loadResourceAsObject(
          "/fixtures/input/connection/connection-2.json", Connection.class);

  static final Connection NEW_CONNECTION =
      FileUtils.loadResourceAsObject(
          "/fixtures/input/connection/connection-3.json", Connection.class);

  static final String FROM_USER_NAME = "From User";

  static final String TO_USER_NAME = "To User";

  static final ConnectionView CONNECTION_VIEW_REQUESTED =
      ConnectionView.builder()
          .connection(CONNECTION_REQUESTED)
          .fromUserName(FROM_USER_NAME)
          .toUserName(TO_USER_NAME)
          .build();

  static final ConnectionView CONNECTION_VIEW_ACCEPTED =
      ConnectionView.builder()
          .connection(CONNECTION_ACCEPTED)
          .fromUserName(FROM_USER_NAME)
          .toUserName(TO_USER_NAME)
          .build();

  static final ConnectionSummary CONNECTION_SUMMARY =
      ConnectionSummary.builder().connected(2).pending(3).rejected(1).build();

  static final String INVITE_CODE = "INV-CODE1";

  static final Invite INVITE_ACTIVE =
      Invite.builder()
          .ownerId(FROM_USER_ID)
          .status(InviteStatus.INVITE_STATUS_ACTIVE)
          .validTo(20991231)
          .maxUseCount(5)
          .usedCount(0)
          .isDeleted(false)
          .build();

  static final BuzzmaUser MEDIATOR_USER = BuzzmaUser.builder().role(UserRole.ROLE_MEDIATOR).build();

  static final BuzzmaUser BUYER_USER =
      BuzzmaUser.builder().id(TO_USER_ID).role(UserRole.ROLE_BUYER).build();

  static final BuzzmaUser BRAND_USER =
      BuzzmaUser.builder().id(TO_USER_ID).role(UserRole.ROLE_BRAND).build();

  private Fixtures() {}
}
