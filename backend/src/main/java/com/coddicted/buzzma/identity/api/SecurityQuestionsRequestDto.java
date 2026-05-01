package com.coddicted.buzzma.identity.api;

import jakarta.validation.constraints.NotBlank;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class SecurityQuestionsRequestDto {

  UUID userId;

  UUID questionId;

  @NotBlank String answer;
}
