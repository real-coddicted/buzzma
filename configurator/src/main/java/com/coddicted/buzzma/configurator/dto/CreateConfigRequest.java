package com.coddicted.buzzma.configurator.dto;

import com.coddicted.buzzma.configurator.enums.ValueTypeEnum;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CreateConfigRequest {

  @NotBlank
  @Size(max = 100)
  private String namespace;

  @NotBlank
  @Size(max = 50)
  private String environment;

  @NotBlank
  @Size(max = 200)
  private String key;

  @NotNull private ValueTypeEnum valueType;

  @NotNull private JsonNode value;

  private String description;

  @Size(max = 100)
  private String owner;
}
