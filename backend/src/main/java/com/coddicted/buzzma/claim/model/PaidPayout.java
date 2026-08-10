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
public class PaidPayout {
  private UUID payeeId;
  private long claimCount;
  private long paymentCount;
  private BigInteger totalAmountPaidPaise;
  private Instant lastPaidAt;
}
