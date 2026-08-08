package com.coddicted.buzzma.claim.dto;

import com.coddicted.buzzma.claim.entity.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecordPaymentRequestDto {

  @NotNull private PaymentMethod paymentMethod;

  @NotNull private Instant paidAt;

  private String utrRef;

  private String notes;

  /**
   * Optional list of ClaimAccounting IDs to include in this batch. When null or empty all pending
   * claims for the payee are included.
   */
  private List<UUID> claimIds;
}
