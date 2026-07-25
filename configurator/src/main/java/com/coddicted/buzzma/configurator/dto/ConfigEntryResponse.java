package com.coddicted.buzzma.configurator.dto;

import com.coddicted.buzzma.configurator.enums.EntryStatusEnum;
import com.coddicted.buzzma.configurator.enums.ValueTypeEnum;
import com.fasterxml.jackson.databind.JsonNode;
import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ConfigEntryResponse {

  private UUID id;
  private String namespace;
  private String environment;
  private String key;
  private ValueTypeEnum valueType;
  private JsonNode value;
  private EntryStatusEnum status;
  private String description;
  private String owner;
  private long changeSeq;
  private Instant createdAt;
  private Instant updatedAt;
  private String updatedBy;
}
