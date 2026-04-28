package com.coddicted.buzzma.identity.api.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class RegisterOpsRequest {

  @NotBlank
  @Size(min = 2, max = 120)
  String name;

  @NotBlank
  @Size(min = 10, max = 10)
  String mobile;

  @NotBlank
  @Size(min = 8, max = 200)
  String password;

  /** One of: agency, mediator */
  @NotBlank String role;

  /** Invite code */
  @NotBlank
  @Size(min = 1, max = 128)
  String code;
}
