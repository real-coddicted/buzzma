package com.coddicted.buzzma.invite.service;

import com.coddicted.buzzma.invite.entity.Invite;
import java.util.UUID;

public interface InviteService {

  Invite getByCode(String inviteCode);

  Invite create(Invite invite, int validityInDays, UUID requesterId);

  void consume(Invite invite, UUID requesterId);

  void delete(UUID id, UUID requesterId);

  boolean verify(String inviteCode);
}
