package com.coddicted.buzzma.configurator.entity;

import com.coddicted.buzzma.configurator.converter.ValueTypeConverter;
import com.coddicted.buzzma.configurator.enums.EntryStatusEnum;
import com.coddicted.buzzma.configurator.enums.EvaluationTypeEnum;
import com.coddicted.buzzma.configurator.enums.ValueTypeEnum;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "config_entries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class ConfigEntry {

  @Id
  @GeneratedValue
  @UuidGenerator
  @Column(name = "id", updatable = false, nullable = false)
  private UUID id;

  @Column(name = "namespace", nullable = false, length = 100)
  private String namespace;

  @Column(name = "environment", nullable = false, length = 50)
  private String environment;

  @Column(name = "key", nullable = false, length = 200)
  private String key;

  @Convert(converter = ValueTypeConverter.class)
  @Column(name = "value_type", columnDefinition = "value_type_enum", nullable = false)
  private ValueTypeEnum valueType;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "value", columnDefinition = "jsonb", nullable = false)
  private JsonNode value;

  @Enumerated(EnumType.STRING)
  @Column(name = "evaluation_type", nullable = false, length = 50)
  @Builder.Default
  private EvaluationTypeEnum evaluationType = EvaluationTypeEnum.STATIC;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "rules", columnDefinition = "jsonb")
  private JsonNode rules;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 50)
  @Builder.Default
  private EntryStatusEnum status = EntryStatusEnum.ACTIVE;

  @Column(name = "description", columnDefinition = "TEXT")
  private String description;

  @Column(name = "owner", length = 100)
  private String owner;

  // Managed by DB: DEFAULT nextval on INSERT, trigger on UPDATE.
  @Column(name = "change_seq", nullable = false, insertable = false, updatable = false)
  private Long changeSeq;

  @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false, insertable = false, updatable = false)
  private Instant updatedAt;

  // Set from authenticated caller identity by the service, never from request body.
  @Column(name = "updated_by", nullable = false, length = 100)
  private String updatedBy;
}
