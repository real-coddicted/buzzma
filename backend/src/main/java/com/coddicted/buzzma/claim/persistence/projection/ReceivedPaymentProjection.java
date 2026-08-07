package com.coddicted.buzzma.claim.persistence.projection;

import java.math.BigInteger;
import java.time.Instant;
import java.util.UUID;

public interface ReceivedPaymentProjection {

  UUID getPaymentId();

  UUID getPayerId();

  Long getClaimCount();

  BigInteger getTotalAmountPaise();

  Instant getPaidAt();
}
