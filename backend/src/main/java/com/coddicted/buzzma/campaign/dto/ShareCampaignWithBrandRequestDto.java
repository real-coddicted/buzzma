package com.coddicted.buzzma.campaign.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class ShareCampaignWithBrandRequestDto {
  @NotNull UUID brandUserId;
}
