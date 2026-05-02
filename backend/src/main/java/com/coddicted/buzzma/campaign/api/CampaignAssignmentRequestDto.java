package com.coddicted.buzzma.campaign.api;

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

  @NotBlank UUID assignorId;

  @NotBlank UUID assigneeId;

  BigInteger campaignPricePaise = BigInteger.ZERO;

  BigInteger commissionOfferedPaise = BigInteger.ZERO;

  Long slotOffered = 0L;
}
