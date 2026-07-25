package com.coddicted.buzzma.configurator.dto;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DeltaPollResponse {

  private String namespace;
  private String environment;
  private long snapshotChangeSeq;
  private int pollIntervalSeconds;
  private List<ConfigEntryResponse> items;
}
