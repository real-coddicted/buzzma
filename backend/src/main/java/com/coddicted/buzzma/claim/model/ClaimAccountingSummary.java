package com.coddicted.buzzma.claim.model;

import java.math.BigInteger;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClaimAccountingSummary {
  private UUID id;
  private UUID claimId;
  private String claimCode;
  private String ecommerceOrderId;
  private UUID campaignId;
  private UUID dealId;
  private BigInteger amountPaise;
  private Instant createdAt;
}
