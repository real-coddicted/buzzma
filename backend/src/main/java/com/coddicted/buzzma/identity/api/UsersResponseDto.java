package com.coddicted.buzzma.identity.api;

import jakarta.annotation.Nullable;
import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class UsersResponseDto {

  UUID id;

  String name;

  @Nullable String username;

  String mobile;

  @Nullable String email;

  String role;

  @Nullable String[] roles;

  String status;

  @Nullable String mediatorCode;

  @Nullable String parentCode;

  @Nullable String[] generatedCodes;

  Boolean isVerifiedByMediator;

  @Nullable String brandCode;

  @Nullable String[] connectedAgencies;

  String kycStatus;

  @Nullable String upiId;

  @Nullable String qrCode;

  @Nullable String bankAccountNumber;

  @Nullable String bankIfsc;

  @Nullable String bankName;

  @Nullable String bankHolderName;

  @Nullable String avatar;

  Boolean isDeleted;

  @Nullable Instant createdAt;

  @Nullable Instant updatedAt;
}
