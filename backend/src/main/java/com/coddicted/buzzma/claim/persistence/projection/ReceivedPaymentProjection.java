package com.coddicted.buzzma.claim.persistence.projection;

import java.math.BigInteger;
import java.time.Instant;
import java.util.UUID;

// Spring Data JPA closed projection — see AwaitedPaymentProjection for details.
public interface ReceivedPaymentProjection {

  UUID getPaymentId();

  UUID getPayerId();

  Long getClaimCount();

  BigInteger getTotalAmountPaise();

  Instant getPaidAt();
}
