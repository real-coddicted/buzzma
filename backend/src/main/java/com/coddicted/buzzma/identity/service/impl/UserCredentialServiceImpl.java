package com.coddicted.buzzma.identity.service.impl;

import com.coddicted.buzzma.identity.entity.UserCredential;
import com.coddicted.buzzma.identity.persistence.UserCredentialRepository;
import com.coddicted.buzzma.identity.service.UserCredentialService;
import com.coddicted.buzzma.shared.common.BaseCrudService;
import com.coddicted.buzzma.shared.common.PasswordService;
import com.coddicted.buzzma.shared.exception.BusinessRuleViolationException;
import com.coddicted.buzzma.shared.exception.NotFoundException;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserCredentialServiceImpl extends BaseCrudService implements UserCredentialService {

  private static final Logger LOGGER = LoggerFactory.getLogger(UserCredentialServiceImpl.class);

  private final UserCredentialRepository credentialRepository;
  private final PasswordService passwordService;

  public UserCredentialServiceImpl(
      final UserCredentialRepository credentialRepository, final PasswordService passwordService) {
    this.credentialRepository = credentialRepository;
    this.passwordService = passwordService;
  }

  @Override
  public UserCredential getByUserId(UUID userId, UUID requesterId) {
    return credentialRepository
        .findByUserIdAndIsDeletedFalse(userId)
        .orElseThrow(() -> new NotFoundException("Credentials not found for user: " + userId));
  }

  @Override
  @Transactional
  public boolean create(final UserCredential credential, final UUID requesterId) {
    credentialRepository.save(
        credential.toBuilder()
            .passwordHash(passwordService.hashPassword(credential.getPasswordHash()))
            .createdBy(requesterId)
            .updatedBy(requesterId)
            .build());
    return true;
  }

  @Override
  @Transactional
  public boolean update(final UserCredential credential, final UUID requesterId) {
    final UserCredential existing =
        credentialRepository
            .findByUserIdAndIsDeletedFalse(credential.getUserId())
            .orElseThrow(
                () ->
                    new NotFoundException(
                        "Credentials not found for user: " + credential.getUserId()));

    if (passwordService.verifyPassword(credential.getPasswordHash(), existing.getPasswordHash())) {
      LOGGER.warn(
          "Password update rejected: new password is same as current for user {}",
          credential.getUserId());
      throw new BusinessRuleViolationException(
          "New password must differ from the current password");
    }

    credentialRepository.save(
        existing.toBuilder()
            .passwordHash(passwordService.hashPassword(credential.getPasswordHash()))
            .updatedBy(requesterId)
            .build());
    return true;
  }

  @Override
  @Transactional(readOnly = true)
  public boolean verify(final UUID userId, final String password) {
    return credentialRepository
        .findByUserIdAndIsDeletedFalse(userId)
        .map(c -> passwordService.verifyPassword(password, c.getPasswordHash()))
        .orElse(false);
  }
}
