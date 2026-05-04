package com.coddicted.buzzma.support.dto;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class TicketRequestDto {

  @NotNull UUID categoryId;
  @NotNull UUID subCategoryId;
  @NotBlank String title;
  @NotBlank String description;
  @Nullable String orderId;
  @Nullable String dealId;
}
