package com.coddicted.buzzma.configsdk.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * One config entry as seen by the SDK. Deliberately narrower than the server's {@code
 * ConfigEntryResponse} — namespace/environment are already known from the fetch context, and
 * description/owner/audit fields are irrelevant to runtime evaluation. Unknown JSON properties are
 * ignored so the server can add fields without breaking older SDK versions.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ConfigEntry {

  private String key;
  private ValueType valueType;
  private JsonNode value;
  private EntryStatus status;
  private long changeSeq;
}
