package com.coddicted.buzzma.connection.dto;

import com.coddicted.buzzma.identity.entity.UserRole;
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

  String fromUserName;

  UserRole fromUserRole;

  String fromUserCode;

  UUID toUserId;

  String toUserName;

  UserRole toUserRole;

  String toUserCode;

  String status;

  UUID createdBy;

  UUID updatedBy;

  Instant createdAt;

  Instant updatedAt;
}
