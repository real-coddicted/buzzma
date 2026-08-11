package com.coddicted.buzzma.identity.service;

import com.coddicted.buzzma.identity.dto.UserBankingDetailDto;
import com.coddicted.buzzma.identity.entity.UserBankingDetail;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;

public interface UserBankingDetailService {
  UserBankingDetail create(UserBankingDetail userBankingDetail, UUID requesterId);

  UserBankingDetail getByUserId(UUID userId);

  /** Banking details keyed by userId, for users that have one; missing users are omitted. */
  Map<UUID, UserBankingDetail> getByUserIds(Collection<UUID> userIds);

  UserBankingDetail update(UUID userId, UserBankingDetailDto dto, UUID requesterId);

  UserBankingDetail upsert(UUID userId, UserBankingDetailDto dto, UUID requesterId);
}
