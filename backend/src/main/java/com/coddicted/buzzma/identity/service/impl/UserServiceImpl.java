package com.coddicted.buzzma.identity.service.impl;

import com.coddicted.buzzma.identity.entity.BuzzmaUser;
import com.coddicted.buzzma.identity.entity.UserRole;
import com.coddicted.buzzma.identity.persistence.UsersRepository;
import com.coddicted.buzzma.identity.service.UserService;
import com.coddicted.buzzma.shared.common.BaseCrudService;
import com.coddicted.buzzma.shared.constants.WellKnownSequences;
import com.coddicted.buzzma.shared.exception.NotFoundException;
import com.coddicted.buzzma.shared.service.CodeGenerationService;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserServiceImpl extends BaseCrudService implements UserService {

  private static final Logger LOG = LoggerFactory.getLogger(UserServiceImpl.class);

  private final UsersRepository repository;
  private final CodeGenerationService codeGenerationService;

  public UserServiceImpl(
      final UsersRepository repository, final CodeGenerationService codeGenerationService) {
    this.repository = repository;
    this.codeGenerationService = codeGenerationService;
  }

  @Override
  @Transactional(readOnly = true)
  public BuzzmaUser getById(final UUID id) {
    return mustFind(this.repository, id, "Users");
  }

  @Override
  @Transactional
  public BuzzmaUser create(final BuzzmaUser entity) {
    final String code =
        this.codeGenerationService.generateCodeFromSequence(WellKnownSequences.USER);
    final BuzzmaUser user = entity.toBuilder().code(code).build();
    return this.repository.save(user);
  }

  @Override
  @Transactional
  public BuzzmaUser update(final BuzzmaUser entity) {
    final BuzzmaUser existingEntity = mustFind(this.repository, entity.getId(), "Users");
    return this.repository.save(entity);
  }

  @Override
  public BuzzmaUser getByMobile(final String mobile) {
    return this.repository
        .findByMobileAndIsDeletedFalse(mobile)
        .orElseThrow(() -> new NotFoundException("user" + " not found: " + mobile));
  }

  @Override
  public void delete(final UUID id, final UUID requesterId) {
    final BuzzmaUser existingEntity = mustFind(this.repository, id, "Users");
    this.repository.save(existingEntity.toBuilder().isDeleted(true).updatedBy(requesterId).build());
  }

  @Override
  @Transactional(readOnly = true)
  public List<BuzzmaUser> getByIds(final List<UUID> ids) {
    return this.repository.findAllById(ids);
  }

  @Override
  @Transactional(readOnly = true)
  public BuzzmaUser getByRole(final UserRole role) {
    return this.repository
        .findFirstByRoleAndIsDeletedFalse(role)
        .orElseThrow(() -> new NotFoundException("No user found with role: " + role));
  }

  @Override
  public boolean existsByMobile(final String mobile) {
    if (this.repository.existsUserByMobileAndIsDeletedFalse(mobile)) {
      LOG.info("User with mobile {} already exists", mobile);
      return true;
    }
    return false;
  }
}
