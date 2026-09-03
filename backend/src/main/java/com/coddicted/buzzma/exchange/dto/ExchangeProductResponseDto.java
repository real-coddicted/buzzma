package com.coddicted.buzzma.exchange.dto;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class ExchangeProductResponseDto {

  UUID id;
  UUID agencyId;
  String name;
  Instant createdAt;
  Instant updatedAt;
}
