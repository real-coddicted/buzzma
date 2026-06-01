package com.coddicted.buzzma.campaign.dto;

import jakarta.validation.constraints.NotBlank;
import java.math.BigInteger;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class CampaignAssignmentRequestDto {

  @NotBlank UUID campaignId;

  @NotBlank UUID assignorId;

  @NotBlank UUID assigneeId;

  @Builder.Default BigInteger adjustedCampaignPricePaise = BigInteger.ZERO;

  @Builder.Default BigInteger commissionOfferedPaise = BigInteger.ZERO;

  @Builder.Default Long slotOffered = 0L;
}
