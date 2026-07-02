package com.coddicted.buzzma.identity.mapper;

import com.coddicted.buzzma.identity.dto.UserBankingDetailDto;
import com.coddicted.buzzma.identity.entity.UserBankingDetail;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserBankingDetailMapper {

  @Mapping(source = "accountNumber", target = "bankAccountNumber")
  @Mapping(source = "accountHolderName", target = "bankAccountHolderName")
  UserBankingDetailDto toDto(UserBankingDetail entity);
}
