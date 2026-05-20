package com.coddicted.buzzma.connection.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class ConnectionRequestDto {

  @NotNull UUID toUserId;
}
