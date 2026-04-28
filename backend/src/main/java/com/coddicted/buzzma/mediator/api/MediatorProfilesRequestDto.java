package com.coddicted.buzzma.mediator.api;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class MediatorProfilesRequestDto {

  UUID userId;

  @NotBlank String mediatorCode;

  @Nullable String parentAgencyCode;

  @Nullable String status;
}
