package com.coddicted.buzzma.identity.dto;

import java.util.Set;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class UserBatchRequestDto {

  Set<UUID> ids;
}
