package com.coddicted.buzzma.claim.model;

import com.coddicted.buzzma.claim.entity.PaymentMethod;
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
public class MadePayment {
  private UUID paymentId;
  private long claimCount;
  private BigInteger totalAmountPaise;
  private Instant paidAt;
  private PaymentMethod paymentMethod;
  private String screenshotStorageKey;
}
