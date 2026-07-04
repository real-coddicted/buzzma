package com.coddicted.buzzma.identity.service;

import com.coddicted.buzzma.identity.dto.UserBankingDetailDto;
import com.coddicted.buzzma.identity.entity.UserBankingDetail;
import java.util.UUID;

public interface UserBankingDetailService {
  UserBankingDetail create(UserBankingDetail userBankingDetail, UUID requesterId);

  UserBankingDetail getByUserId(UUID userId);

  UserBankingDetail update(UUID userId, UserBankingDetailDto dto, UUID requesterId);

  UserBankingDetail upsert(UUID userId, UserBankingDetailDto dto, UUID requesterId);
}
