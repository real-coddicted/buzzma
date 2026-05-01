package com.coddicted.buzzma.identity.service;

import com.coddicted.buzzma.identity.entity.Invite;

import java.util.UUID;

public interface InviteBusinessService {

  Invite consumeInvite(String code, String role, UUID usedByUserId);

  Invite revokeInvite(String code, UUID revokedByUserId);
}
