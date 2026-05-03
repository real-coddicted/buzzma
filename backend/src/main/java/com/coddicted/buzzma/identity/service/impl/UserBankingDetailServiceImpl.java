package com.coddicted.buzzma.identity.service.impl;

import com.coddicted.buzzma.identity.entity.UserBankingDetail;
import com.coddicted.buzzma.identity.persistence.UserBankingDetailRepository;
import com.coddicted.buzzma.identity.service.UserBankingDetailService;
import com.coddicted.buzzma.shared.common.BaseCrudService;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class UserBankingDetailServiceImpl extends BaseCrudService
    implements UserBankingDetailService {

  private final UserBankingDetailRepository userBankingDetailRepository;

  public UserBankingDetailServiceImpl(
      final UserBankingDetailRepository userBankingDetailRepository) {
    this.userBankingDetailRepository = userBankingDetailRepository;
  }

  @Override
  public UserBankingDetail create(
      final UserBankingDetail userBankingDetail, final UUID requesterId) {
    return userBankingDetailRepository.save(
        userBankingDetail.toBuilder().createdBy(requesterId).updatedBy(requesterId).build());
  }
}
