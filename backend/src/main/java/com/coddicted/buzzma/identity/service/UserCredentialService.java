package com.coddicted.buzzma.identity.service;

import com.coddicted.buzzma.identity.entity.UserCredential;
import java.util.UUID;

public interface UserCredentialService {
  UserCredential getByUserId(UUID userId, UUID requesterId);

  boolean create(UserCredential credential, UUID requesterId);

  boolean update(UserCredential credential, UUID requesterId);

  boolean verify(UUID userId, String password);
}
