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
public class PaymentReceipt {
  private UUID id;
  private UUID payerId;
  private UUID payeeId;
  private BigInteger amountPaidPaise;
  private long claimCount;
  private PaymentMethod paymentMethod;
  private String utrRef;
  private String notes;
  private String screenshotStorageKey;
  private Instant paidAt;
}
