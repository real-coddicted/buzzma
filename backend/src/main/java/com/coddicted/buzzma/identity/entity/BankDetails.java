package com.coddicted.buzzma.identity.entity;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class BankDetails {
  String accountNumber;
  String bankIfscCode;
  String bankName;
  String accountHolderName;
}
