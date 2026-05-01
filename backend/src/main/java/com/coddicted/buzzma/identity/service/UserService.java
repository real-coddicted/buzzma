package com.coddicted.buzzma.identity.service;

import com.coddicted.buzzma.identity.entity.BuzzmaUser;

import java.util.UUID;

public interface UserService {

    BuzzmaUser getById(UUID id);

    BuzzmaUser create(BuzzmaUser user);

    BuzzmaUser update(UUID id, BuzzmaUser user, UUID requesterId);

    void delete(UUID id, UUID requesterId);
}
