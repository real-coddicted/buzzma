package com.coddicted.buzzma.identity.mapper;

import com.coddicted.buzzma.identity.dto.UserBankingDetailDto;
import com.coddicted.buzzma.identity.entity.UserBankingDetail;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserBankingDetailMapper {

  int VISIBLE_ACCOUNT_DIGITS = 4;

  @Mapping(source = "bankDetails.accountNumber", target = "bankAccountNumber")
  @Mapping(source = "bankDetails.bankIfscCode", target = "bankIfscCode")
  @Mapping(source = "bankDetails.bankName", target = "bankName")
  @Mapping(source = "bankDetails.accountHolderName", target = "bankAccountHolderName")
  @Mapping(source = "upiDetails.upiId", target = "upiId")
  @Mapping(source = "upiDetails.mobileNumber", target = "upiMobileNumber")
  UserBankingDetailDto toDto(UserBankingDetail entity);

  /** Same as {@link #toDto} but with the account number shown as "XX" plus its last four digits. */
  default UserBankingDetailDto toMaskedDto(final UserBankingDetail entity) {
    final UserBankingDetailDto dto = toDto(entity);
    final String accountNumber = dto.getBankAccountNumber();
    if (accountNumber == null || accountNumber.length() <= VISIBLE_ACCOUNT_DIGITS) {
      return dto;
    }
    final int maskedLength = accountNumber.length() - VISIBLE_ACCOUNT_DIGITS;
    return dto.toBuilder().bankAccountNumber("XX" + accountNumber.substring(maskedLength)).build();
  }
}
