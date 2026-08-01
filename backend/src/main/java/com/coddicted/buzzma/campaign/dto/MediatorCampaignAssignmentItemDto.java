package com.coddicted.buzzma.campaign.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigInteger;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class MediatorCampaignAssignmentItemDto {
  @NotNull UUID campaignId;
  @NotNull UUID campaignSlotId;
  @NotNull @PositiveOrZero BigInteger commissionOfferedPaise;
  @NotNull @PositiveOrZero BigInteger adjustedCampaignPricePaise;
  @NotNull @Positive Integer totalSlots;
}
