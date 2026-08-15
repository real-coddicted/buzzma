package com.coddicted.buzzma.communications.email.dto;

import com.coddicted.buzzma.communications.email.model.EmailStatus;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class SendEmailResponseDto {

  UUID requestId;
  EmailStatus status;
}
