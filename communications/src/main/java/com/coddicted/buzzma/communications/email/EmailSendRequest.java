package com.coddicted.buzzma.communications.email;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class EmailSendRequest {

  String to;
  String subject;
  String body;
}
