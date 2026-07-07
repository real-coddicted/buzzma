package com.coddicted.buzzma.campaign.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigInteger;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class CampaignAssignmentRequestDto {

  UUID campaignId;

  @NotNull UUID assignorId;

  @NotNull UUID assigneeId;

  @Builder.Default BigInteger adjustedCampaignPricePaise = BigInteger.ZERO;

  @Builder.Default BigInteger commissionOfferedPaise = BigInteger.ZERO;

  @PositiveOrZero @Builder.Default Long slotOffered = 0L;
}
