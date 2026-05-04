package com.coddicted.buzzma.support.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class TicketAssignRequestDto {
  @NotNull UUID assigneeId;
}
