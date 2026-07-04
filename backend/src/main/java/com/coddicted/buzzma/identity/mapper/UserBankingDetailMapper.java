package com.coddicted.buzzma.identity.mapper;

import com.coddicted.buzzma.identity.dto.UserBankingDetailDto;
import com.coddicted.buzzma.identity.entity.UserBankingDetail;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserBankingDetailMapper {

  @Mapping(source = "bankDetails.accountNumber", target = "bankAccountNumber")
  @Mapping(source = "bankDetails.bankIfscCode", target = "bankIfscCode")
  @Mapping(source = "bankDetails.bankName", target = "bankName")
  @Mapping(source = "bankDetails.accountHolderName", target = "bankAccountHolderName")
  @Mapping(source = "upiDetails.upiId", target = "upiId")
  @Mapping(source = "upiDetails.mobileNumber", target = "upiMobileNumber")
  UserBankingDetailDto toDto(UserBankingDetail entity);
}
