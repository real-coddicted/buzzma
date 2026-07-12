package com.coddicted.buzzma.connection.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class CreateConnectionRequestDto {
  @NotNull String inviteCode;
}
