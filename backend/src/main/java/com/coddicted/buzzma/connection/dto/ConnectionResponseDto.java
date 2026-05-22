package com.coddicted.buzzma.connection.dto;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class ConnectionResponseDto {

  UUID id;

  UUID fromUserId;

  String fromName;

  UUID toUserId;

  String toName;

  String status;

  UUID createdBy;

  UUID updatedBy;

  Instant createdAt;

  Instant updatedAt;
}
