package com.coddicted.buzzma.identity.api.auth;

import jakarta.annotation.Nullable;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class UpdateProfileRequest {

  @Nullable String name;

  @Nullable String email;

  @Nullable String avatar;

  @Nullable String upiId;

  @Nullable String qrCode;

  @Nullable String bankAccountNumber;

  @Nullable String bankIfsc;

  @Nullable String bankName;

  @Nullable String bankHolderName;
}
