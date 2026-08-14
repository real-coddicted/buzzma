package com.coddicted.buzzma.configsdk.snapshot;

import com.coddicted.buzzma.configsdk.model.ConfigEntry;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * On-disk shape of a per-{@code namespace+environment} snapshot (design doc §7 "Disk snapshot").
 * {@code formatVersion} lets future SDK versions — Java or otherwise, see design doc §8 — detect
 * and reject a snapshot file shape they don't understand instead of misparsing it.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SnapshotFile {

  public static final int CURRENT_FORMAT_VERSION = 1;

  private int formatVersion;
  private String namespace;
  private String environment;
  private long snapshotChangeSeq;
  private Instant writtenAt;
  private List<ConfigEntry> items;
}
