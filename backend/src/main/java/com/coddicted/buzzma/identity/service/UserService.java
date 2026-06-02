package com.coddicted.buzzma.identity.service;

import com.coddicted.buzzma.identity.entity.BuzzmaUser;
import java.util.List;
import java.util.UUID;

public interface UserService {

  BuzzmaUser getById(UUID id);

  List<BuzzmaUser> getByIds(List<UUID> ids);

  // Todo: pass requesterId in write methods
  BuzzmaUser create(BuzzmaUser user);

  BuzzmaUser update(BuzzmaUser user);

  BuzzmaUser getByMobile(String mobile);

  void delete(UUID id, UUID requesterId);

  boolean existsByMobile(String mobile);
}
