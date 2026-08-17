package com.coddicted.buzzma.shared.communications;

import java.util.UUID;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class SendEmailResponseDto {
  UUID requestId;
  String status;
}
