package com.coddicted.buzzma.connection.dto;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class ConnectionSummaryResponseDto {

  long total;

  long connected;

  long pending;

  long rejected;
}
