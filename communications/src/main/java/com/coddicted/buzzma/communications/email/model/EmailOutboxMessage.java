package com.coddicted.buzzma.communications.email.model;

import java.util.UUID;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class EmailOutboxMessage {

  UUID requestId;
  String to;
  String subject;
  String body;
}
