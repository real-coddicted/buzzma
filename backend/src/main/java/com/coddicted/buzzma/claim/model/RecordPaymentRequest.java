package com.coddicted.buzzma.claim.model;

import com.coddicted.buzzma.claim.entity.PaymentMethod;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecordPaymentRequest {
  private PaymentMethod paymentMethod;
  private Instant paidAt;
  private String utrRef;
  private String notes;
  private List<UUID> claimIds;
}
