package com.coddicted.buzzma.invite.service.impl;

import com.coddicted.buzzma.identity.entity.UserRole;
import com.coddicted.buzzma.identity.service.UserService;
import com.coddicted.buzzma.invite.entity.Invite;
import com.coddicted.buzzma.invite.entity.InviteStatus;
import com.coddicted.buzzma.invite.persistence.InviteRepository;
import com.coddicted.buzzma.invite.service.InviteService;
import com.coddicted.buzzma.shared.common.BaseCrudService;
import com.coddicted.buzzma.shared.constants.WellKnownSequences;
import com.coddicted.buzzma.shared.exception.BusinessRuleViolationException;
import com.coddicted.buzzma.shared.exception.NotFoundException;
import com.coddicted.buzzma.shared.service.CodeGenerationService;
import com.coddicted.buzzma.shared.util.DateTimeUtils;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InviteServiceImpl extends BaseCrudService implements InviteService {

  private static final Logger LOG = LoggerFactory.getLogger(InviteServiceImpl.class);

  private static final int DEFAULT_MAX_USE_COUNT = 1;

  private static final int DEFAULT_VALIDITY_DAYS = 1;

  private static final Map<UserRole, Integer> ROLE_MAX_USE_COUNT =
      Map.of(
          UserRole.ROLE_MEDIATOR, 100,
          UserRole.ROLE_ADMIN, 1,
          UserRole.ROLE_BRAND, 1,
          UserRole.ROLE_AGENCY, 1,
          UserRole.ROLE_BUYER, 0);

  private final InviteRepository inviteRepository;
  private final CodeGenerationService codeGenerationService;
  private final UserService userService;

  public InviteServiceImpl(
      final InviteRepository inviteRepository,
      final CodeGenerationService codeGenerationService,
      final UserService userService) {
    this.inviteRepository = inviteRepository;
    this.codeGenerationService = codeGenerationService;
    this.userService = userService;
  }

  @Override
  @Transactional(readOnly = true)
  public Invite getByCode(final String inviteCode) {
    return fetchByCode(inviteCode);
  }

  @Override
  @Transactional
  public Invite create(final Invite invite, final int validityInDays, final UUID requesterId) {
    final String code = generateUniqueCode();
    final int days = validityInDays > 0 ? validityInDays : DEFAULT_VALIDITY_DAYS;
    final int validTo = DateTimeUtils.toIntDate(LocalDate.now().plusDays(days));
    final int maxUseCount =
        invite.getMaxUseCount() > 0
            ? invite.getMaxUseCount()
            : defaultMaxUseCount(this.userService.getById(requesterId).getRole());
    return this.inviteRepository.save(
        invite.toBuilder()
            .code(code)
            .status(InviteStatus.INVITE_STATUS_ACTIVE)
            .validTo(validTo)
            .maxUseCount(maxUseCount)
            .usedCount(0)
            .ownerId(requesterId)
            .createdBy(requesterId)
            .updatedBy(requesterId)
            .build());
  }

  @Override
  @Transactional
  public void consume(final Invite invite, final UUID requesterId) {
    isActive(invite);
    final int usedCount = invite.getUsedCount() + 1;
    final InviteStatus status =
        usedCount >= invite.getMaxUseCount()
            ? InviteStatus.INVITE_STATUS_USED
            : InviteStatus.INVITE_STATUS_ACTIVE;
    this.inviteRepository.save(
        invite.toBuilder().usedCount(usedCount).status(status).updatedBy(requesterId).build());
    LOG.debug("Invite consumed: code={}, usedCount={}", invite.getCode(), usedCount);
  }

  @Override
  @Transactional
  public void delete(final UUID id, final UUID requesterId) {
    final Invite invite = mustFind(this.inviteRepository, id, "Invite");
    this.inviteRepository.save(invite.toBuilder().isDeleted(true).updatedBy(requesterId).build());
  }

  @Override
  @Transactional(readOnly = true)
  public boolean verify(final String inviteCode) {
    final Invite invite = fetchByCode(inviteCode);
    try {
      isActive(invite);
      return true;
    } catch (final BusinessRuleViolationException e) {
      return false;
    }
  }

  @Override
  public void isActive(final Invite invite) {
    if (invite.getIsDeleted()) {
      LOG.warn("Invite validation failed: invite {} is deleted", invite.getCode());
      throw new BusinessRuleViolationException("Invite is not valid");
    }
    if (invite.getStatus() != InviteStatus.INVITE_STATUS_ACTIVE) {
      LOG.warn(
          "Invite validation failed: invite {} has status {}",
          invite.getCode(),
          invite.getStatus());
      throw new BusinessRuleViolationException("Invite is not active");
    }
    final LocalDate validTo = DateTimeUtils.toLocalDate(invite.getValidTo());
    if (validTo.isBefore(LocalDate.now())) {
      LOG.warn("Invite validation failed: invite {} expired on {}", invite.getCode(), validTo);
      throw new BusinessRuleViolationException("Invite has expired");
    }
    if (invite.getUsedCount() >= invite.getMaxUseCount()) {
      LOG.warn(
          "Invite validation failed: invite {} reached its usage limit of {}",
          invite.getCode(),
          invite.getMaxUseCount());
      throw new BusinessRuleViolationException("Invite has reached its usage limit");
    }
  }

  private Invite fetchByCode(final String inviteCode) {
    return this.inviteRepository
        .findByCodeAndIsDeletedFalse(inviteCode)
        .orElseThrow(() -> new NotFoundException("Invite not found: " + inviteCode));
  }

  private int defaultMaxUseCount(final UserRole role) {
    return ROLE_MAX_USE_COUNT.getOrDefault(role, DEFAULT_MAX_USE_COUNT);
  }

  private String generateUniqueCode() {
    String code;
    do {
      code = this.codeGenerationService.generateCodeFromSequence(WellKnownSequences.INVITE);
    } while (this.inviteRepository.existsByCode(code));
    return code;
  }
}
