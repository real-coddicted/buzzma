package com.coddicted.buzzma.identity.service;

import com.coddicted.buzzma.identity.entity.InviteEntity;

import java.util.UUID;

public interface InviteBusinessService {

  InviteEntity consumeInvite(String code, String role, UUID usedByUserId);

  InviteEntity revokeInvite(String code, UUID revokedByUserId);
}
