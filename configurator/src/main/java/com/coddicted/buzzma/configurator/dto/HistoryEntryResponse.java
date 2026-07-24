package com.coddicted.buzzma.configurator.dto;

import com.coddicted.buzzma.configurator.enums.EntryStatusEnum;
import com.fasterxml.jackson.databind.JsonNode;
import java.time.Instant;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class HistoryEntryResponse {

  private long historyId;
  private String key;
  private JsonNode oldValue;
  private JsonNode newValue;
  private EntryStatusEnum oldStatus;
  private EntryStatusEnum newStatus;
  private long changeSeq;
  private Instant changedAt;
  private String changedBy;
  private String changeReason;
}
