package com.coddicted.buzzma.support.dto;

import java.util.UUID;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class TicketSubCategoryResponseDto {

  UUID id;
  UUID categoryId;
  String name;
  String code;
}
