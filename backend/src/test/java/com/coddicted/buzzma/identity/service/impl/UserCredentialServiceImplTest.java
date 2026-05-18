package com.coddicted.buzzma.identity.service.impl;

import static com.coddicted.buzzma.identity.service.impl.Fixtures.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.coddicted.buzzma.identity.entity.UserCredential;
import com.coddicted.buzzma.identity.persistence.UserCredentialRepository;
import com.coddicted.buzzma.shared.common.PasswordService;
import com.coddicted.buzzma.shared.exception.BusinessRuleViolationException;
import com.coddicted.buzzma.shared.exception.NotFoundException;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserCredentialServiceImplTest {

  @Mock private UserCredentialRepository mockCredentialRepository;
  @Mock private PasswordService mockPasswordService;
  private UserCredentialServiceImpl userCredentialService;

  @BeforeEach
  void setUp() {
    this.userCredentialService =
        new UserCredentialServiceImpl(this.mockCredentialRepository, this.mockPasswordService);
  }

  @Test
  void testGetByUserIdWhenFound() {
    when(this.mockCredentialRepository.findByUserIdAndIsDeletedFalse(USER_ID))
        .thenReturn(Optional.of(USER_CREDENTIAL_2));

    assertEquals(USER_CREDENTIAL_2, this.userCredentialService.getByUserId(USER_ID, REQUESTER_ID));
  }

  @Test
  void testGetByUserIdWhenNotFound() {
    when(this.mockCredentialRepository.findByUserIdAndIsDeletedFalse(USER_ID))
        .thenReturn(Optional.empty());

    final NotFoundException ex =
        assertThrows(
            NotFoundException.class,
            () -> this.userCredentialService.getByUserId(USER_ID, REQUESTER_ID));
    assertEquals("Credentials not found for user: " + USER_ID, ex.getMessage());
  }

  @Test
  void testCreate() {
    when(this.mockPasswordService.hashPassword(PLAIN_PASSWORD)).thenReturn(NEW_HASH);

    final boolean result = this.userCredentialService.create(USER_CREDENTIAL_1, REQUESTER_ID);

    assertTrue(result);
    final ArgumentCaptor<UserCredential> captor = ArgumentCaptor.forClass(UserCredential.class);
    verify(this.mockCredentialRepository).save(captor.capture());
    final UserCredential saved = captor.getValue();
    assertEquals(NEW_HASH, saved.getPasswordHash());
    assertEquals(REQUESTER_ID, saved.getCreatedBy());
    assertEquals(REQUESTER_ID, saved.getUpdatedBy());
  }

  @Test
  void testUpdateWhenFound() {
    when(this.mockCredentialRepository.findByUserIdAndIsDeletedFalse(USER_ID))
        .thenReturn(Optional.of(USER_CREDENTIAL_2));
    when(this.mockPasswordService.verifyPassword(PLAIN_PASSWORD, STORED_HASH)).thenReturn(false);
    when(this.mockPasswordService.hashPassword(PLAIN_PASSWORD)).thenReturn(NEW_HASH);

    final boolean result = this.userCredentialService.update(USER_CREDENTIAL_1, REQUESTER_ID);

    assertTrue(result);
    final ArgumentCaptor<UserCredential> captor = ArgumentCaptor.forClass(UserCredential.class);
    verify(this.mockCredentialRepository).save(captor.capture());
    final UserCredential saved = captor.getValue();
    assertEquals(NEW_HASH, saved.getPasswordHash());
    assertEquals(REQUESTER_ID, saved.getUpdatedBy());
  }

  @Test
  void testUpdateWhenNotFound() {
    when(this.mockCredentialRepository.findByUserIdAndIsDeletedFalse(USER_ID))
        .thenReturn(Optional.empty());

    final NotFoundException ex =
        assertThrows(
            NotFoundException.class,
            () -> this.userCredentialService.update(USER_CREDENTIAL_1, REQUESTER_ID));
    assertEquals("Credentials not found for user: " + USER_ID, ex.getMessage());
  }

  @Test
  void testUpdateWhenSamePassword() {
    when(this.mockCredentialRepository.findByUserIdAndIsDeletedFalse(USER_ID))
        .thenReturn(Optional.of(USER_CREDENTIAL_2));
    when(this.mockPasswordService.verifyPassword(PLAIN_PASSWORD, STORED_HASH)).thenReturn(true);

    final BusinessRuleViolationException ex =
        assertThrows(
            BusinessRuleViolationException.class,
            () -> this.userCredentialService.update(USER_CREDENTIAL_1, REQUESTER_ID));
    assertEquals("New password must differ from the current password", ex.getMessage());
  }

  @Test
  void testVerifyWhenPasswordMatches() {
    when(this.mockCredentialRepository.findByUserIdAndIsDeletedFalse(USER_ID))
        .thenReturn(Optional.of(USER_CREDENTIAL_2));
    when(this.mockPasswordService.verifyPassword(PLAIN_PASSWORD, STORED_HASH)).thenReturn(true);

    assertTrue(this.userCredentialService.verify(USER_ID, PLAIN_PASSWORD));
  }

  @Test
  void testVerifyWhenPasswordDoesNotMatch() {
    when(this.mockCredentialRepository.findByUserIdAndIsDeletedFalse(USER_ID))
        .thenReturn(Optional.of(USER_CREDENTIAL_2));
    when(this.mockPasswordService.verifyPassword(PLAIN_PASSWORD, STORED_HASH)).thenReturn(false);

    assertFalse(this.userCredentialService.verify(USER_ID, PLAIN_PASSWORD));
  }

  @Test
  void testVerifyWhenCredentialNotFound() {
    when(this.mockCredentialRepository.findByUserIdAndIsDeletedFalse(USER_ID))
        .thenReturn(Optional.empty());

    assertFalse(this.userCredentialService.verify(USER_ID, PLAIN_PASSWORD));
  }
}
