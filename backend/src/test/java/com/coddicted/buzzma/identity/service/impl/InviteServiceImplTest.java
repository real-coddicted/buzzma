package com.coddicted.buzzma.identity.service.impl;

import static com.coddicted.buzzma.identity.service.impl.Fixtures.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import com.coddicted.buzzma.identity.service.UserService;
import com.coddicted.buzzma.invite.entity.Invite;
import com.coddicted.buzzma.invite.entity.InviteStatus;
import com.coddicted.buzzma.invite.persistence.InviteRepository;
import com.coddicted.buzzma.invite.service.impl.InviteServiceImpl;
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
  @Mock private UserService mockUserService;
  private InviteServiceImpl inviteService;

  @BeforeEach
  void setUp() {
    this.inviteService =
        new InviteServiceImpl(
            this.mockInviteRepository, this.mockCodeGenerator, this.mockUserService);
  }

  @Test
  void testGetByCodeWhenFound() {
    doReturn(Optional.of(INVITE_2))
        .when(this.mockInviteRepository)
        .findByCodeAndIsDeletedFalse(INVITE_CODE);

    assertEquals(INVITE_2, this.inviteService.getByCode(INVITE_CODE));
  }

  @Test
  void testGetByCodeWhenNotFound() {
    doReturn(Optional.empty())
        .when(this.mockInviteRepository)
        .findByCodeAndIsDeletedFalse(INVITE_CODE);

    final NotFoundException ex =
        assertThrows(NotFoundException.class, () -> this.inviteService.getByCode(INVITE_CODE));
    assertEquals("Invite not found: " + INVITE_CODE, ex.getMessage());
  }

  @Test
  void testCreateUsesRoleDefaultMaxUseCount() {
    doReturn(GENERATED_CODE).when(this.mockCodeGenerator).generateHumanCode("INV");
    doReturn(false).when(this.mockInviteRepository).existsByCode(GENERATED_CODE);
    doReturn(USER_1).when(this.mockUserService).getById(REQUESTER_ID);

    this.inviteService.create(INVITE_1, 0, REQUESTER_ID);

    final ArgumentCaptor<Invite> captor = ArgumentCaptor.forClass(Invite.class);
    verify(this.mockInviteRepository).save(captor.capture());
    final Invite saved = captor.getValue();
    assertEquals(GENERATED_CODE, saved.getCode());
    assertEquals(InviteStatus.INVITE_STATUS_ACTIVE, saved.getStatus());
    assertEquals(100, saved.getMaxUseCount());
    assertEquals(0, saved.getUsedCount());
    assertEquals(REQUESTER_ID, saved.getOwnerId());
    assertEquals(REQUESTER_ID, saved.getCreatedBy());
    assertEquals(REQUESTER_ID, saved.getUpdatedBy());
  }

  @Test
  void testCreateUsesExplicitMaxUseCount() {
    doReturn(GENERATED_CODE).when(this.mockCodeGenerator).generateHumanCode("INV");
    doReturn(false).when(this.mockInviteRepository).existsByCode(GENERATED_CODE);

    this.inviteService.create(INVITE_6, 0, REQUESTER_ID);

    final ArgumentCaptor<Invite> captor = ArgumentCaptor.forClass(Invite.class);
    verify(this.mockInviteRepository).save(captor.capture());
    assertEquals(7, captor.getValue().getMaxUseCount());
  }

  @Test
  void testCreateWithCodeCollision() {
    final String collidingCode = "INV-COLLISION";
    doReturn(collidingCode, GENERATED_CODE).when(this.mockCodeGenerator).generateHumanCode("INV");
    doReturn(true).when(this.mockInviteRepository).existsByCode(collidingCode);
    doReturn(false).when(this.mockInviteRepository).existsByCode(GENERATED_CODE);
    doReturn(USER_1).when(this.mockUserService).getById(REQUESTER_ID);

    this.inviteService.create(INVITE_1, 0, REQUESTER_ID);

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
    assertEquals(1, saved.getUsedCount());
    assertEquals(InviteStatus.INVITE_STATUS_ACTIVE, saved.getStatus());
    assertEquals(REQUESTER_ID, saved.getUpdatedBy());
  }

  @Test
  void testConsumeFlipsToUsedOnFinalUse() {
    this.inviteService.consume(INVITE_8, REQUESTER_ID);

    final ArgumentCaptor<Invite> captor = ArgumentCaptor.forClass(Invite.class);
    verify(this.mockInviteRepository).save(captor.capture());
    final Invite saved = captor.getValue();
    assertEquals(1, saved.getUsedCount());
    assertEquals(InviteStatus.INVITE_STATUS_USED, saved.getStatus());
  }

  @Test
  void testConsumeWhenLimitReached() {
    final BusinessRuleViolationException ex =
        assertThrows(
            BusinessRuleViolationException.class,
            () -> this.inviteService.consume(INVITE_7, REQUESTER_ID));
    assertEquals("Invite has reached its usage limit", ex.getMessage());
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
    doReturn(Optional.of(INVITE_2)).when(this.mockInviteRepository).findById(INVITE_ID);

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
    doReturn(Optional.empty()).when(this.mockInviteRepository).findById(INVITE_ID);

    final NotFoundException ex =
        assertThrows(
            NotFoundException.class, () -> this.inviteService.delete(INVITE_ID, REQUESTER_ID));
    assertEquals("Invite not found: " + INVITE_ID, ex.getMessage());
  }

  @Test
  void testVerifyWhenActive() {
    doReturn(Optional.of(INVITE_2))
        .when(this.mockInviteRepository)
        .findByCodeAndIsDeletedFalse(INVITE_CODE);

    assertTrue(this.inviteService.verify(INVITE_CODE));
  }

  @Test
  void testVerifyWhenNotActive() {
    doReturn(Optional.of(INVITE_4))
        .when(this.mockInviteRepository)
        .findByCodeAndIsDeletedFalse("INV-USED");

    assertFalse(this.inviteService.verify("INV-USED"));
  }

  @Test
  void testVerifyWhenLimitReached() {
    doReturn(Optional.of(INVITE_7))
        .when(this.mockInviteRepository)
        .findByCodeAndIsDeletedFalse("INV-LIMIT");

    assertFalse(this.inviteService.verify("INV-LIMIT"));
  }

  @Test
  void testVerifyWhenNotFound() {
    doReturn(Optional.empty())
        .when(this.mockInviteRepository)
        .findByCodeAndIsDeletedFalse(INVITE_CODE);

    final NotFoundException ex =
        assertThrows(NotFoundException.class, () -> this.inviteService.verify(INVITE_CODE));
    assertEquals("Invite not found: " + INVITE_CODE, ex.getMessage());
  }
}
