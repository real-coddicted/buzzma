package com.coddicted.buzzma.identity.service.impl;

import com.coddicted.buzzma.identity.entity.UserCredential;
import com.coddicted.buzzma.identity.persistence.UserCredentialRepository;
import com.coddicted.buzzma.shared.common.PasswordService;
import com.coddicted.buzzma.shared.exception.BusinessRuleViolationException;
import com.coddicted.buzzma.shared.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static com.coddicted.buzzma.identity.service.impl.Fixtures.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserCredentialServiceImplTest {

    @Mock
    private UserCredentialRepository mockCredentialRepository;
    @Mock
    private PasswordService mockPasswordService;
    private UserCredentialServiceImpl userCredentialService;

    @BeforeEach
    void setUp() {
        this.userCredentialService =
                new UserCredentialServiceImpl(this.mockCredentialRepository, this.mockPasswordService);
    }

    @Test
    void testGetByUserIdWhenFound() {
        doReturn(Optional.of(USER_CREDENTIAL_2))
                .when(this.mockCredentialRepository)
                .findByUserIdAndIsDeletedFalse(USER_ID);

        assertEquals(
                USER_CREDENTIAL_2, this.userCredentialService.getByUserId(USER_ID, REQUESTER_ID));
    }

    @Test
    void testGetByUserIdWhenNotFound() {
        doReturn(Optional.empty())
                .when(this.mockCredentialRepository)
                .findByUserIdAndIsDeletedFalse(USER_ID);

        final NotFoundException ex =
                assertThrows(
                        NotFoundException.class,
                        () -> this.userCredentialService.getByUserId(USER_ID, REQUESTER_ID));
        assertEquals("Credentials not found for user: " + USER_ID, ex.getMessage());
    }

    @Test
    void testCreate() {
        doReturn(NEW_HASH).when(this.mockPasswordService).hashPassword(PLAIN_PASSWORD);

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
        doReturn(Optional.of(USER_CREDENTIAL_2))
                .when(this.mockCredentialRepository)
                .findByUserIdAndIsDeletedFalse(USER_ID);
        doReturn(false)
                .when(this.mockPasswordService)
                .verifyPassword(PLAIN_PASSWORD, STORED_HASH);
        doReturn(NEW_HASH).when(this.mockPasswordService).hashPassword(PLAIN_PASSWORD);

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
        doReturn(Optional.empty())
                .when(this.mockCredentialRepository)
                .findByUserIdAndIsDeletedFalse(USER_ID);

        final NotFoundException ex =
                assertThrows(
                        NotFoundException.class,
                        () -> this.userCredentialService.update(USER_CREDENTIAL_1, REQUESTER_ID));
        assertEquals("Credentials not found for user: " + USER_ID, ex.getMessage());
    }

    @Test
    void testUpdateWhenSamePassword() {

        doReturn(Optional.of(USER_CREDENTIAL_2))
                .when(this.mockCredentialRepository)
                .findByUserIdAndIsDeletedFalse(USER_ID);

        doReturn(true)
                .when(this.mockPasswordService)
                .verifyPassword(PLAIN_PASSWORD, STORED_HASH);

        final BusinessRuleViolationException ex =
                assertThrows(
                        BusinessRuleViolationException.class,
                        () -> this.userCredentialService.update(USER_CREDENTIAL_1, REQUESTER_ID));
        assertEquals("New password must differ from the current password", ex.getMessage());
    }

    @Test
    void testVerifyWhenPasswordMatches() {
        doReturn(Optional.of(USER_CREDENTIAL_2))
                .when(this.mockCredentialRepository)
                .findByUserIdAndIsDeletedFalse(USER_ID);
        doReturn(NEW_HASH).when(this.mockPasswordService).hashPassword(PLAIN_PASSWORD);
        doReturn(true).when(this.mockPasswordService).verifyPassword(NEW_HASH, STORED_HASH);

        assertTrue(this.userCredentialService.verify(USER_ID, PLAIN_PASSWORD));
    }

    @Test
    void testVerifyWhenPasswordDoesNotMatch() {
        doReturn(Optional.of(USER_CREDENTIAL_2))
                .when(this.mockCredentialRepository)
                .findByUserIdAndIsDeletedFalse(USER_ID);
        doReturn(NEW_HASH).when(this.mockPasswordService).hashPassword(PLAIN_PASSWORD);
        doReturn(false).when(this.mockPasswordService).verifyPassword(NEW_HASH, STORED_HASH);

        assertFalse(this.userCredentialService.verify(USER_ID, PLAIN_PASSWORD));
    }

    @Test
    void testVerifyWhenCredentialNotFound() {
        doReturn(Optional.empty())
                .when(this.mockCredentialRepository)
                .findByUserIdAndIsDeletedFalse(USER_ID);

        assertFalse(this.userCredentialService.verify(USER_ID, PLAIN_PASSWORD));
    }
}
