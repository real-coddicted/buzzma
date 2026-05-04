package com.coddicted.buzzma.support.dto;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class TicketCommentResponseDto {

  UUID id;
  UUID ticketId;
  UUID authorId;
  String content;
  Instant createdAt;
}
