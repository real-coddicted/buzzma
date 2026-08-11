package com.coddicted.buzzma.claim.dto;

import java.math.BigInteger;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClaimAccountingSummaryDto {

  /** ClaimAccounting.id — pass this as a claimId in the pay request. */
  private UUID id;

  private UUID claimId;
  private String claimCode;
  private String ecommerceOrderId;
  private UUID campaignId;
  private UUID dealId;
  private BigInteger amountPaise;
  private Instant createdAt;
}
