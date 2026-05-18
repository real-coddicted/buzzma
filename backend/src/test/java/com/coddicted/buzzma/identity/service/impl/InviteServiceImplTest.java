package com.coddicted.buzzma.identity.service.impl;

import static com.coddicted.buzzma.identity.service.impl.Fixtures.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.coddicted.buzzma.identity.entity.Invite;
import com.coddicted.buzzma.identity.entity.InviteStatus;
import com.coddicted.buzzma.identity.entity.UserRole;
import com.coddicted.buzzma.identity.persistence.InviteRepository;
import com.coddicted.buzzma.shared.common.CodeGenerator;
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
class InviteServiceImplTest {

  @Mock private InviteRepository mockInviteRepository;
  @Mock private CodeGenerator mockCodeGenerator;
  private InviteServiceImpl inviteService;

  @BeforeEach
  void setUp() {
    this.inviteService = new InviteServiceImpl(this.mockInviteRepository, this.mockCodeGenerator);
  }

  @Test
  void testGetByRoleAndCodeWhenFound() {
    when(this.mockInviteRepository.findByRoleAndCodeAndIsDeletedFalse(
            UserRole.ROLE_BUYER, INVITE_CODE))
        .thenReturn(Optional.of(INVITE_2));

    assertEquals(INVITE_2, this.inviteService.getByRoleAndCode(UserRole.ROLE_BUYER, INVITE_CODE));
  }

  @Test
  void testGetByRoleAndCodeWhenNotFound() {
    when(this.mockInviteRepository.findByRoleAndCodeAndIsDeletedFalse(
            UserRole.ROLE_BUYER, INVITE_CODE))
        .thenReturn(Optional.empty());

    final NotFoundException ex =
        assertThrows(
            NotFoundException.class,
            () -> this.inviteService.getByRoleAndCode(UserRole.ROLE_BUYER, INVITE_CODE));
    assertEquals("Invite not found: " + INVITE_CODE, ex.getMessage());
  }

  @Test
  void testCreate() {
    when(this.mockCodeGenerator.generateHumanCode("INV")).thenReturn(GENERATED_CODE);
    when(this.mockInviteRepository.existsByCode(GENERATED_CODE)).thenReturn(false);

    this.inviteService.create(INVITE_1, REQUESTER_ID);

    final ArgumentCaptor<Invite> captor = ArgumentCaptor.forClass(Invite.class);
    verify(this.mockInviteRepository).save(captor.capture());
    final Invite saved = captor.getValue();
    assertEquals(GENERATED_CODE, saved.getCode());
    assertEquals(InviteStatus.INVITE_STATUS_ACTIVE, saved.getStatus());
    assertEquals(REQUESTER_ID, saved.getOwnerId());
    assertEquals(REQUESTER_ID, saved.getCreatedBy());
    assertEquals(REQUESTER_ID, saved.getUpdatedBy());
  }

  @Test
  void testCreateWithCodeCollision() {
    final String collidingCode = "INV-COLLISION";
    when(this.mockCodeGenerator.generateHumanCode("INV")).thenReturn(collidingCode, GENERATED_CODE);
    when(this.mockInviteRepository.existsByCode(collidingCode)).thenReturn(true);
    when(this.mockInviteRepository.existsByCode(GENERATED_CODE)).thenReturn(false);

    this.inviteService.create(INVITE_1, REQUESTER_ID);

    final ArgumentCaptor<Invite> captor = ArgumentCaptor.forClass(Invite.class);
    verify(this.mockInviteRepository).save(captor.capture());
    assertEquals(GENERATED_CODE, captor.getValue().getCode());
  }

  @Test
  void testConsumeWhenValid() {
    this.inviteService.consume(INVITE_2, REQUESTER_ID);

    final ArgumentCaptor<Invite> captor = ArgumentCaptor.forClass(Invite.class);
    verify(this.mockInviteRepository).save(captor.capture());
    final Invite saved = captor.getValue();
    assertEquals(InviteStatus.INVITE_STATUS_USED, saved.getStatus());
    assertEquals(REQUESTER_ID, saved.getUpdatedBy());
  }

  @Test
  void testConsumeWhenDeleted() {
    final BusinessRuleViolationException ex =
        assertThrows(
            BusinessRuleViolationException.class,
            () -> this.inviteService.consume(INVITE_5, REQUESTER_ID));
    assertEquals("Invite is not valid", ex.getMessage());
  }

  @Test
  void testConsumeWhenNotActive() {
    final BusinessRuleViolationException ex =
        assertThrows(
            BusinessRuleViolationException.class,
            () -> this.inviteService.consume(INVITE_4, REQUESTER_ID));
    assertEquals("Invite is not active", ex.getMessage());
  }

  @Test
  void testConsumeWhenExpired() {
    final BusinessRuleViolationException ex =
        assertThrows(
            BusinessRuleViolationException.class,
            () -> this.inviteService.consume(INVITE_3, REQUESTER_ID));
    assertEquals("Invite has expired", ex.getMessage());
  }

  @Test
  void testDeleteWhenFound() {
    when(this.mockInviteRepository.findById(INVITE_ID)).thenReturn(Optional.of(INVITE_2));

    this.inviteService.delete(INVITE_ID, REQUESTER_ID);

    final ArgumentCaptor<Invite> captor = ArgumentCaptor.forClass(Invite.class);
    verify(this.mockInviteRepository).save(captor.capture());
    final Invite saved = captor.getValue();
    assertEquals(INVITE_ID, saved.getId());
    assertTrue(saved.getIsDeleted());
    assertEquals(REQUESTER_ID, saved.getUpdatedBy());
  }

  @Test
  void testDeleteWhenNotFound() {
    when(this.mockInviteRepository.findById(INVITE_ID)).thenReturn(Optional.empty());

    final NotFoundException ex =
        assertThrows(
            NotFoundException.class, () -> this.inviteService.delete(INVITE_ID, REQUESTER_ID));
    assertEquals("Invite not found: " + INVITE_ID, ex.getMessage());
  }

  @Test
  void testVerifyWhenActive() {
    when(this.mockInviteRepository.findByRoleAndCodeAndIsDeletedFalse(
            UserRole.ROLE_BUYER, INVITE_CODE))
        .thenReturn(Optional.of(INVITE_2));

    assertTrue(this.inviteService.verify(UserRole.ROLE_BUYER, INVITE_CODE));
  }

  @Test
  void testVerifyWhenNotActive() {
    when(this.mockInviteRepository.findByRoleAndCodeAndIsDeletedFalse(
            UserRole.ROLE_BUYER, "INV-USED"))
        .thenReturn(Optional.of(INVITE_4));

    assertFalse(this.inviteService.verify(UserRole.ROLE_BUYER, "INV-USED"));
  }

  @Test
  void testVerifyWhenNotFound() {
    when(this.mockInviteRepository.findByRoleAndCodeAndIsDeletedFalse(
            UserRole.ROLE_BUYER, INVITE_CODE))
        .thenReturn(Optional.empty());

    final NotFoundException ex =
        assertThrows(
            NotFoundException.class,
            () -> this.inviteService.verify(UserRole.ROLE_BUYER, INVITE_CODE));
    assertEquals("Invite not found: " + INVITE_CODE, ex.getMessage());
  }
}
