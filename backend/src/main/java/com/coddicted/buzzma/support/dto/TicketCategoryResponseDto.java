package com.coddicted.buzzma.support.dto;

import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder(toBuilder = true)
@Jacksonized
public class TicketCategoryResponseDto {

  UUID id;
  String name;
  String code;
  Boolean requiresOrderId;
  Boolean requiresDealId;
  List<TicketSubCategoryResponseDto> subCategories;
}
