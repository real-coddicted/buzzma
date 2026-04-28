package com.coddicted.buzzma.identity.api.auth;

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
public class ForgotPasswordResetRequest {

  @NotBlank
  @Size(min = 10, max = 10)
  String mobile;

  @NotNull
  @Size(min = 3, max = 3)
  List<SecurityQuestionsRequest.QuestionAnswer> answers;

  @NotBlank
  @Size(min = 8, max = 200)
  String newPassword;
}
