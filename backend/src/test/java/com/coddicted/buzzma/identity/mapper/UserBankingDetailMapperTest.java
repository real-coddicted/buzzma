package com.coddicted.buzzma.identity.mapper;

import static com.coddicted.buzzma.identity.mapper.Fixtures.EXPECTED_BANKING_DTO_1;
import static com.coddicted.buzzma.identity.mapper.Fixtures.EXPECTED_MASKED_BANKING_DTO_1;
import static com.coddicted.buzzma.identity.mapper.Fixtures.USER_BANKING_DETAIL_1;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.coddicted.buzzma.identity.entity.UserBankingDetail;
import org.junit.jupiter.api.Test;

class UserBankingDetailMapperTest {

  private final UserBankingDetailMapper mapper = new UserBankingDetailMapperImpl();

  @Test
  void testToDtoKeepsFullAccountNumber() {
    assertEquals(EXPECTED_BANKING_DTO_1, this.mapper.toDto(USER_BANKING_DETAIL_1));
  }

  @Test
  void testToMaskedDtoMasksAllButLastFourAccountDigits() {
    assertEquals(EXPECTED_MASKED_BANKING_DTO_1, this.mapper.toMaskedDto(USER_BANKING_DETAIL_1));
  }

  @Test
  void testToMaskedDtoWithoutBankDetailsLeavesAccountNumberNull() {
    final UserBankingDetail upiOnly = USER_BANKING_DETAIL_1.toBuilder().bankDetails(null).build();

    assertNull(this.mapper.toMaskedDto(upiOnly).getBankAccountNumber());
  }
}
