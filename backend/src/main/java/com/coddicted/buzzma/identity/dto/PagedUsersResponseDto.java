package com.coddicted.buzzma.identity.dto;

import java.util.List;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class PagedUsersResponseDto {
  List<UserSummaryDto> items;
  long total;
  int page;
  int totalPages;
}
