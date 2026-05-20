package com.coddicted.buzzma.support.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class TicketCommentRequestDto {


  @NotBlank
  @Size(max = 2000)
  String content;
}
