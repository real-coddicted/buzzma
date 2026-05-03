package com.coddicted.buzzma.identity.dto;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class UserRequestDto {

  @NotBlank
  @Size(max = 120)
  String name;

  @Nullable String username;

  @NotBlank
  @Size(min = 10, max = 10)
  String mobile;

  @Nullable String email;

  @NotBlank String passwordHash;

  @Nullable String role;

  @Nullable String[] roles;

  @Nullable String status;

  @Nullable String mediatorCode;

  @Nullable String parentCode;

  @Nullable String[] generatedCodes;

  @Nullable Boolean isVerifiedByMediator;

  @Nullable String brandCode;

  @Nullable String[] connectedAgencies;

  @Nullable String kycStatus;

  @Nullable String upiId;

  @Nullable String qrCode;

  @Nullable String bankAccountNumber;

  @Nullable String bankIfsc;

  @Nullable String bankName;

  @Nullable String bankHolderName;

  @Nullable String avatar;
}
