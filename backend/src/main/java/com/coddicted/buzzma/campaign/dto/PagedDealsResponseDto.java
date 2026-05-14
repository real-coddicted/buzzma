package com.coddicted.buzzma.campaign.dto;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

@Value
@Builder
@Jacksonized
public class PagedDealsResponseDto {
  List<DealResponseDto> items;
  long total;
  int page;
  int totalPages;
}
