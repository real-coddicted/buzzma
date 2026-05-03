package com.coddicted.buzzma.identity.service;

import com.coddicted.buzzma.identity.entity.UserBankingDetail;

import java.util.UUID;

public interface UserBankingDetailService {
    UserBankingDetail create(UserBankingDetail userBankingDetail, UUID requesterId);
}
