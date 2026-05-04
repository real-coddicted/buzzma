package com.coddicted.buzzma.support.dto;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class TicketAttachmentResponseDto {

  UUID id;
  UUID ticketId;
  String fileName;
  String contentType;
  Long sizeBytes;
  Instant createdAt;
}
