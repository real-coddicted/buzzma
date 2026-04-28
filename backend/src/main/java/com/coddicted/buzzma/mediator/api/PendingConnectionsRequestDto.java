package com.coddicted.buzzma.mediator.api;

import jakarta.annotation.Nullable;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class PendingConnectionsRequestDto {

  UUID userId;

  @Nullable String agencyId;

  @Nullable String agencyName;

  @Nullable String agencyCode;
}
