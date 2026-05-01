package com.coddicted.buzzma.identity.service;

import com.coddicted.buzzma.identity.entity.Invite;
import com.coddicted.buzzma.identity.entity.UserRole;

import java.util.UUID;

public interface InviteService {

    Invite create(Invite invite);

    boolean consume(UserRole inviteeRole, String inviteCode, UUID requesterId);

    void delete(UUID id, UUID requesterId);
}
