package com.coddicted.buzzma.identity.dto;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class UserBankingDetailDto {

  String bankAccountNumber;

  String bankIfscCode;

  String bankName;

  String bankAccountHolderName;

  String upiId;

  String upiMobileNumber;
}
