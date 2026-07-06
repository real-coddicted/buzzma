package com.coddicted.buzzma.identity.entity;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class UpiDetails {
  String upiId;
  String mobileNumber;
}
