package com.coddicted.buzzma.identity.service.impl;

import com.coddicted.buzzma.identity.entity.BuzzmaUser;
import com.coddicted.buzzma.identity.persistence.UsersRepository;
import com.coddicted.buzzma.identity.service.UserService;
import com.coddicted.buzzma.shared.common.BaseCrudService;
import com.coddicted.buzzma.shared.exception.NotFoundException;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserServiceImpl extends BaseCrudService implements UserService {

  private final UsersRepository repository;

  public UserServiceImpl(UsersRepository repository) {
    this.repository = repository;
  }

  @Override
  @Transactional(readOnly = true)
  public BuzzmaUser getById(UUID id) {
    return mustFind(repository, id, "Users");
  }

  @Override
  @Transactional
  public BuzzmaUser create(BuzzmaUser entity) {
    return repository.save(entity);
  }

  @Override
  @Transactional
  public BuzzmaUser update(BuzzmaUser entity) {
    BuzzmaUser existingEntity = mustFind(repository, entity.getId(), "Users");
    return repository.save(entity);
  }

  @Override
  public BuzzmaUser getByMobile(String mobile) {
    return this.repository
        .findByMobileAndIsDeletedFalse(mobile)
        .orElseThrow(() -> new NotFoundException("user" + " not found: " + mobile));
  }

  @Override
  public void delete(UUID id, UUID requesterId) {
    BuzzmaUser existingEntity = mustFind(repository, id, "Users");
    repository.save(existingEntity.toBuilder().isDeleted(true).updatedBy(requesterId).build());
  }

  @Override
  @Transactional(readOnly = true)
  public List<BuzzmaUser> getByIds(List<UUID> ids) {
    return repository.findAllById(ids);
  }

  @Override
  public boolean existsByMobile(String mobile) {
    return this.repository.existsUserByMobileAndIsDeletedFalse(mobile);
  }
}
