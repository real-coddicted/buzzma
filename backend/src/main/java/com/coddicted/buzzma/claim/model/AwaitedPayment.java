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
public class AwaitedPayment {
  private UUID counterpartyId;
  private long claimCount;
  private BigInteger totalAmountPaise;
  private Instant oldestClaimAt;
}
