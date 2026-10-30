package com.coddicted.buzzma.identity.mapper;

import com.coddicted.buzzma.identity.dto.UserBankingDetailDto;
import com.coddicted.buzzma.identity.entity.UserBankingDetail;
import com.coddicted.buzzma.shared.util.FileUtils;

public final class Fixtures {

  static final UserBankingDetail USER_BANKING_DETAIL_1 =
      FileUtils.loadResourceAsObject(
          "/fixtures/input/identity/user-banking-detail-1.json", UserBankingDetail.class);

  static final UserBankingDetailDto EXPECTED_BANKING_DTO_1 =
      FileUtils.loadResourceAsObject(
          "/fixtures/output/identity/user-banking-detail-dto-1.json", UserBankingDetailDto.class);

  static final UserBankingDetailDto EXPECTED_MASKED_BANKING_DTO_1 =
      FileUtils.loadResourceAsObject(
          "/fixtures/output/identity/user-banking-detail-dto-masked-1.json",
          UserBankingDetailDto.class);

  private Fixtures() {}
}
