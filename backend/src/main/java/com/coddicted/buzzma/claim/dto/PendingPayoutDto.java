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
public class PendingPayoutDto {

  private UUID payeeId;
  private long claimCount;
  private BigInteger totalAmountPaise;
  private Instant oldestClaimAt;
}
