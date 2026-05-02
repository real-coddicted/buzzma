package com.coddicted.buzzma.identity.service.impl;

import com.coddicted.buzzma.identity.entity.Invite;
import com.coddicted.buzzma.identity.entity.InviteStatus;
import com.coddicted.buzzma.identity.entity.UserRole;
import com.coddicted.buzzma.identity.persistence.InviteRepository;
import com.coddicted.buzzma.identity.service.InviteService;
import com.coddicted.buzzma.shared.common.BaseCrudService;
import com.coddicted.buzzma.shared.common.CodeGenerator;
import com.coddicted.buzzma.shared.util.DateTimeUtils;
import java.time.LocalDate;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InviteServiceImpl extends BaseCrudService implements InviteService {

  private static final Logger log = LoggerFactory.getLogger(InviteServiceImpl.class);

  private final InviteRepository inviteRepository;
  private final CodeGenerator codeGenerator;

  public InviteServiceImpl(
      final InviteRepository inviteRepository, final CodeGenerator codeGenerator) {
    this.inviteRepository = inviteRepository;
    this.codeGenerator = codeGenerator;
  }

  @Override
  @Transactional
  public Invite create(final Invite invite) {
    return inviteRepository.save(
        invite.toBuilder()
            .code(codeGenerator.generateHumanCode("INV"))
            .status(InviteStatus.INVITE_STATUS_ACTIVE)
            .build());
  }

  @Override
  @Transactional
  public boolean consume(
      final UserRole inviteeRole, final String inviteCode, final UUID requesterId) {
    final Invite invite =
        inviteRepository
            .findByCodeAndIsDeletedFalse(inviteCode)
            .orElseThrow(() -> new RuntimeException("Invite not found"));
    if (!verify(inviteeRole, invite)) {
      return false;
    }
    inviteRepository.save(
        invite.toBuilder().status(InviteStatus.INVITE_STATUS_USED).updatedBy(requesterId).build());
    return true;
  }

  @Override
  @Transactional
  public void delete(final UUID id, final UUID requesterId) {
    final Invite invite = mustFind(inviteRepository, id, "Invite");
    inviteRepository.save(invite.toBuilder().updatedBy(requesterId).isDeleted(true).build());
  }

  @Override
  public boolean verify(UserRole inviteeRole, String inviteCode, UUID requesterId) {
    return false;
  }

  private boolean verify(final UserRole inviteeRole, final Invite existingInvite) {
    log.debug("Verifying invite code={} role={}", existingInvite.getCode(), inviteeRole);

    if (existingInvite.getIsDeleted()) {
      log.warn("Invite verification failed: invite {} is deleted", existingInvite.getCode());
      return false;
    }
    if (existingInvite.getStatus() != InviteStatus.INVITE_STATUS_ACTIVE) {
      log.warn(
          "Invite verification failed: invite {} has status {}",
          existingInvite.getCode(),
          existingInvite.getStatus());
      return false;
    }
    final LocalDate validTo = DateTimeUtils.toLocalDate(existingInvite.getValidTo());
    if (validTo.isBefore(LocalDate.now())) {
      log.warn(
          "Invite verification failed: invite {} expired on {}", existingInvite.getCode(), validTo);
      return false;
    }
    if (existingInvite.getRole() != inviteeRole) {
      log.warn(
          "Invite verification failed: invite {} role {} does not match requested role {}",
          existingInvite.getCode(),
          existingInvite.getRole(),
          inviteeRole);
      return false;
    }

    log.debug("Invite verification succeeded for code={}", existingInvite.getCode());
    return true;
  }
}
