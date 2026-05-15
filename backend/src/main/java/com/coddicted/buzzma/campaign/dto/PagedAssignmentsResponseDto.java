package com.coddicted.buzzma.campaign.dto;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

@Value
@Builder
@Jacksonized
public class PagedAssignmentsResponseDto {
  List<AssignmentResponseDto> items;
  long total;
  int page;
  int totalPages;
}
