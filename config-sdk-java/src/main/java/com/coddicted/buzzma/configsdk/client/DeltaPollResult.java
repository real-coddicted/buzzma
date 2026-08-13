package com.coddicted.buzzma.configsdk.client;

import com.coddicted.buzzma.configsdk.model.ConfigEntry;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Deserialized {@code GET /v1/configs/delta} response — mirrors the server's DeltaPollResponse.
 * {@code items} includes DELETED-status entries so the poller knows what to drop, not just what to
 * add/update.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class DeltaPollResult {

  private String namespace;
  private String environment;
  private long snapshotChangeSeq;
  private int pollIntervalSeconds;
  private List<ConfigEntry> items;
}
