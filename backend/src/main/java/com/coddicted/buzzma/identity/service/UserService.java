package com.coddicted.buzzma.identity.service;

import com.coddicted.buzzma.identity.entity.BuzzmaUser;
import com.coddicted.buzzma.identity.entity.UserRole;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface UserService {

  BuzzmaUser getById(UUID id);

  List<BuzzmaUser> getByIds(List<UUID> ids);

  /** Of {@code ids}, only those users directly connected (as parent or child) to {@code userId}. */
  List<BuzzmaUser> getConnectedByIds(List<UUID> ids, UUID userId);

  /** Map of user id to name, for the given {@code ids}. */
  Map<UUID, String> getNamesByIds(Collection<UUID> ids);

  // Todo: pass requesterId in write methods
  BuzzmaUser create(BuzzmaUser user);

  BuzzmaUser update(BuzzmaUser user);

  BuzzmaUser updateProfile(String email, UUID requesterId);

  BuzzmaUser getByMobile(String mobile);

  BuzzmaUser getByRole(UserRole role);

  void delete(UUID id, UUID requesterId);

  boolean existsByMobile(String mobile);
}
