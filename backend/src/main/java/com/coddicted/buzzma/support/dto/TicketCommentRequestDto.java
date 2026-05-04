package com.coddicted.buzzma.support.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class TicketCommentRequestDto {

  @NotBlank UUID ticketId;

  @NotBlank
  @Size(max = 2000)
  String content;
}
