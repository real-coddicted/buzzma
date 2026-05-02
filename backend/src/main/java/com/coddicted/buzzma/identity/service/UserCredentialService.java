package com.coddicted.buzzma.identity.service;

import com.coddicted.buzzma.identity.entity.UserCredential;

import java.util.UUID;


public interface UserCredentialService {
    boolean create(UserCredential credential, UUID requesterId);

    boolean update(UUID userId, String password, UUID requesterId);

    boolean verify(UUID userId, String password);
}
