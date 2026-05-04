package com.coddicted.buzzma.support.dto;

import com.coddicted.buzzma.support.entity.TicketStatus;
import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class TicketResponseDto {

  UUID id;
  UUID raisedBy;
  UUID categoryId;
  UUID subCategoryId;
  String title;
  String description;
  String orderId;
  String dealId;
  TicketStatus status;
  UUID assigneeId;
  Instant closedAt;
  UUID createdBy;
  UUID updatedBy;
  Instant createdAt;
  Instant updatedAt;
}
