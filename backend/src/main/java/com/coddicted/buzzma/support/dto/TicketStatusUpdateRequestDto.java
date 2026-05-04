package com.coddicted.buzzma.support.dto;

import com.coddicted.buzzma.support.entity.TicketStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class TicketStatusUpdateRequestDto {
  @NotNull TicketStatus status;
}
