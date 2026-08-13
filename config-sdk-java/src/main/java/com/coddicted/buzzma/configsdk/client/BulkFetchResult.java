package com.coddicted.buzzma.configsdk.client;

import com.coddicted.buzzma.configsdk.model.ConfigEntry;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Deserialized {@code GET /v1/configs} response — mirrors the server's BulkFetchResponse. */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class BulkFetchResult {

  private String namespace;
  private String environment;
  private long snapshotChangeSeq;
  private List<ConfigEntry> items;
}
