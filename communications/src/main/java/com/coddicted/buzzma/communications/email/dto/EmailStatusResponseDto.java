package com.coddicted.buzzma.communications.email.dto;

import com.coddicted.buzzma.communications.email.model.EmailStatus;
import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class EmailStatusResponseDto {

  UUID requestId;
  String to;
  String from;
  String subject;
  EmailStatus status;
  Instant requestedAt;
  Instant sentAt;
  String errorMessage;
}
