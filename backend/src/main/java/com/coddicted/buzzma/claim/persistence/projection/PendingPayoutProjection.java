package com.coddicted.buzzma.claim.persistence.projection;

import java.math.BigInteger;
import java.time.Instant;
import java.util.UUID;

public interface PendingPayoutProjection {

  UUID getPayeeId();

  Long getClaimCount();

  BigInteger getTotalAmountPaise();

  Instant getOldestClaimAt();
}
