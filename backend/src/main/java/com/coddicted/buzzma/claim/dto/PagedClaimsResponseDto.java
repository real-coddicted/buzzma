package com.coddicted.buzzma.claim.dto;

import java.util.List;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class PagedClaimsResponseDto {
  List<ClaimResponseDto> items;
  long total;
  int page;
  int totalPages;
}
