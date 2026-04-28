package com.coddicted.buzzma.identity.api.auth;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class UserSummary {

  UUID id;

  String name;

  String mobile;

  String email;

  String role;

  String[] roles;

  String status;

  String mediatorCode;

  String parentCode;

  Boolean isVerifiedByMediator;

  String brandCode;

  String kycStatus;

  String upiId;

  String bankAccountNumber;

  String bankIfsc;

  String bankName;

  String bankHolderName;

  String avatar;

  Integer walletAvailablePaise;

  Integer walletPendingPaise;

  Instant createdAt;
}
