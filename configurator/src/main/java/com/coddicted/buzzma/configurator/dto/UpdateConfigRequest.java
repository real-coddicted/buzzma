package com.coddicted.buzzma.configurator.dto;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UpdateConfigRequest {

  @NotNull private JsonNode value;

  @NotNull private Long expectedChangeSeq;

  private String description;

  private String owner;
}
