package com.coddicted.buzzma.campaign.api;

import java.math.BigInteger;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class CommissionResponseDto {
  UUID campaignId;
  UUID chargedById;
  BigInteger commissionPaise;
}
