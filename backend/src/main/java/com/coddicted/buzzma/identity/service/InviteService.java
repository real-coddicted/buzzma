package com.coddicted.buzzma.identity.service;

import com.coddicted.buzzma.identity.entity.Invite;
import com.coddicted.buzzma.identity.entity.UserRole;
import java.util.UUID;

public interface InviteService {

  Invite getByRoleAndCode(UserRole inviteeRole, String inviteCode);

  Invite create(Invite invite, UUID requesterId);

  void consume(Invite invite, UUID requesterId);

  void delete(UUID id, UUID requesterId);

  boolean verify(UserRole inviteeRole, String inviteCode);
}
