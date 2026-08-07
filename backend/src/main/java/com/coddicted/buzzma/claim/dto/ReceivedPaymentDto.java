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
public class ReceivedPaymentDto {

  private UUID paymentId;
  private UUID payerId;
  private long claimCount;
  private BigInteger totalAmountPaise;
  private Instant paidAt;
  private PaymentMethod paymentMethod;
  private String screenshotStorageKey;
}
