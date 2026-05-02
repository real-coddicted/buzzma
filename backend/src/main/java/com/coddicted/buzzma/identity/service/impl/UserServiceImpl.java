package com.coddicted.buzzma.identity.service.impl;

import com.coddicted.buzzma.identity.entity.BuzzmaUser;
import com.coddicted.buzzma.identity.mapper.UsersMapper;
import com.coddicted.buzzma.identity.persistence.UsersRepository;
import com.coddicted.buzzma.identity.service.UserService;
import com.coddicted.buzzma.shared.common.BaseCrudService;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserServiceImpl extends BaseCrudService implements UserService {

  private final UsersRepository repository;
  private final UsersMapper mapper;

  public UserServiceImpl(UsersRepository repository, UsersMapper mapper) {
    this.repository = repository;
    this.mapper = mapper;
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
}
