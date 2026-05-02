package com.coddicted.buzzma.identity.api.auth;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class SecurityQuestionWrapper {

  @Min(1)
  @Max(7)
  UUID questionId;

  @NotBlank
  @Size(min = 1, max = 200)
  String answer;
}
