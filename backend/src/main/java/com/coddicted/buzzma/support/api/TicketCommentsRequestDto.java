package com.coddicted.buzzma.support.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class TicketCommentsRequestDto {

  UUID ticketId;

  UUID userId;

  @NotBlank String userName;

  @NotBlank String role;

  @NotBlank
  @Size(max = 2000)
  String message;
}
