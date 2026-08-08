package com.coddicted.buzzma.claim.dto;

import com.coddicted.buzzma.claim.entity.PaymentMethod;
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
public class PaymentReceiptDto {

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
