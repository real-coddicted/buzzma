package com.coddicted.buzzma.identity.api.auth;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class SecurityQuestionsRequest {

  @NotNull
  @Size(min = 3, max = 3)
  List<QuestionAnswer> questions;

  @Value
  @Builder
  @Jacksonized
  public static class QuestionAnswer {

    @Min(1)
    @Max(7)
    int questionId;

    @NotBlank
    @Size(min = 1, max = 200)
    String answer;
  }
}
