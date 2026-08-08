package com.coddicted.buzzma.claim.persistence.projection;

import java.math.BigInteger;
import java.time.Instant;
import java.util.UUID;

// Spring Data JPA closed projection: declares the columns a @Query should return (via matching
// getters) so JPA maps the JPQL/aggregate result directly into this interface, skipping the cost
// of loading full ClaimAccounting entities.
public interface AwaitedPaymentProjection {

  UUID getCounterpartyId();

  Long getClaimCount();

  BigInteger getTotalAmountPaise();

  Instant getOldestClaimAt();
}
