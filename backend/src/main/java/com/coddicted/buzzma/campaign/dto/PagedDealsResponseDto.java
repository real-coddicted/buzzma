package com.coddicted.buzzma.campaign.dto;

import java.util.List;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class PagedDealsResponseDto {
  List<DealResponseDto> items;
  long total;
  int page;
  int totalPages;
}
