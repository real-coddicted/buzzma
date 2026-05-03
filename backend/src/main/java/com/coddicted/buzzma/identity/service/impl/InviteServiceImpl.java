package com.coddicted.buzzma.identity.service.impl;

import com.coddicted.buzzma.identity.constant.WellKnownInvitePrefix;
import com.coddicted.buzzma.identity.entity.Invite;
import com.coddicted.buzzma.identity.entity.InviteStatus;
import com.coddicted.buzzma.identity.entity.UserRole;
import com.coddicted.buzzma.identity.persistence.InviteRepository;
import com.coddicted.buzzma.identity.service.InviteService;
import com.coddicted.buzzma.shared.common.BaseCrudService;
import com.coddicted.buzzma.shared.common.CodeGenerator;
import com.coddicted.buzzma.shared.exception.BusinessRuleViolationException;
import com.coddicted.buzzma.shared.exception.NotFoundException;
import com.coddicted.buzzma.shared.util.DateTimeUtils;
import java.time.LocalDate;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InviteServiceImpl extends BaseCrudService implements InviteService {

  private static final Logger LOG = LoggerFactory.getLogger(InviteServiceImpl.class);

  private final InviteRepository inviteRepository;
  private final CodeGenerator codeGenerator;

  public InviteServiceImpl(
      final InviteRepository inviteRepository, final CodeGenerator codeGenerator) {
    this.inviteRepository = inviteRepository;
    this.codeGenerator = codeGenerator;
  }

  @Override
  @Transactional(readOnly = true)
  public Invite getByRoleAndCode(final UserRole inviteeRole, final String inviteCode) {
    return inviteRepository
        .findByRoleAndCodeAndIsDeletedFalse(inviteeRole, inviteCode)
        .orElseThrow(() -> new NotFoundException("Invite not found: " + inviteCode));
  }

  @Override
  @Transactional
  public Invite create(final Invite invite, final UUID requesterId) {
    final String code = generateUniqueCode();
    return inviteRepository.save(
        invite.toBuilder()
            .code(code)
            .status(InviteStatus.INVITE_STATUS_ACTIVE)
            .ownerId(requesterId)
            .createdBy(requesterId)
            .updatedBy(requesterId)
            .build());
  }

  @Override
  @Transactional
  public void consume(final Invite invite, final UUID requesterId) {
    if (invite.getIsDeleted()) {
      LOG.warn("Invite verification failed: invite {} is deleted", invite.getCode());
      throw new BusinessRuleViolationException("Invite is not valid");
    }
    if (invite.getStatus() != InviteStatus.INVITE_STATUS_ACTIVE) {
      LOG.warn(
          "Invite verification failed: invite {} has status {}",
          invite.getCode(),
          invite.getStatus());
      throw new BusinessRuleViolationException("Invite is not active");
    }
    final LocalDate validTo = DateTimeUtils.toLocalDate(invite.getValidTo());
    if (validTo.isBefore(LocalDate.now())) {
      LOG.warn("Invite verification failed: invite {} expired on {}", invite.getCode(), validTo);
      throw new BusinessRuleViolationException("Invite has expired");
    }
    inviteRepository.save(
        invite.toBuilder().status(InviteStatus.INVITE_STATUS_USED).updatedBy(requesterId).build());
    LOG.debug("Invite consumed: code={}", invite.getCode());
  }

  @Override
  @Transactional
  public void delete(final UUID id, final UUID requesterId) {
    final Invite invite = mustFind(inviteRepository, id, "Invite");
    inviteRepository.save(invite.toBuilder().isDeleted(true).updatedBy(requesterId).build());
  }

  @Override
  public boolean verify(UserRole inviteeRole, String inviteCode) {
    return !inviteRepository.existsByRoleAndCodeAndIsDeletedFalse(inviteeRole, inviteCode);
  }

  private String generateUniqueCode() {
    String code;
    do {
      code = codeGenerator.generateHumanCode(WellKnownInvitePrefix.GENERAL_INVITE_PREFIX);
    } while (inviteRepository.existsByCode(code));
    return code;
  }
}
