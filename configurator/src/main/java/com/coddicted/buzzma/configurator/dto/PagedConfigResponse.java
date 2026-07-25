package com.coddicted.buzzma.configurator.dto;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PagedConfigResponse {

  private List<ConfigEntryResponse> items;
  private long total;
  private int page;
  private int totalPages;
}
