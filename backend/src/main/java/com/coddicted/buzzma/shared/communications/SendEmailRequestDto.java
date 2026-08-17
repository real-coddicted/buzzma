package com.coddicted.buzzma.shared.communications;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class SendEmailRequestDto {
  String to;
  String subject;
  String body;
  String requestedBy;
}
