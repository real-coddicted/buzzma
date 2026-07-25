package com.coddicted.buzzma.configurator.entity;

import com.coddicted.buzzma.configurator.enums.EntryStatusEnum;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "config_entries_history")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ConfigEntryHistory {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "history_id")
  private Long historyId;

  @Column(name = "entry_id", nullable = false)
  private UUID entryId;

  @Column(name = "namespace", nullable = false, length = 100)
  private String namespace;

  @Column(name = "environment", nullable = false, length = 50)
  private String environment;

  @Column(name = "key", nullable = false, length = 200)
  private String key;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "old_value", columnDefinition = "jsonb")
  private JsonNode oldValue;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "new_value", columnDefinition = "jsonb")
  private JsonNode newValue;

  @Enumerated(EnumType.STRING)
  @Column(name = "old_status", length = 50)
  private EntryStatusEnum oldStatus;

  @Enumerated(EnumType.STRING)
  @Column(name = "new_status", length = 50)
  private EntryStatusEnum newStatus;

  @Column(name = "change_seq", nullable = false)
  private Long changeSeq;

  @Column(name = "changed_at", nullable = false)
  private Instant changedAt;

  @Column(name = "changed_by", nullable = false, length = 100)
  private String changedBy;

  @Column(name = "change_reason", columnDefinition = "TEXT")
  private String changeReason;
}
