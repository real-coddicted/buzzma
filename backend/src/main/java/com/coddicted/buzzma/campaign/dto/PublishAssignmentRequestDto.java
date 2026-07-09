package com.coddicted.buzzma.campaign.dto;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import java.math.BigInteger;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class PublishAssignmentRequestDto {
  @NotNull UUID campaignId;
  @NotNull BigInteger commissionChargedPaise;
  @NotNull BigInteger dealPricePaise;
  @Nullable String affiliateUrl;
}
