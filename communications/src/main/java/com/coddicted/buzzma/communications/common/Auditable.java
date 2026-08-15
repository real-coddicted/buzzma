package com.coddicted.buzzma.communications.common;

import java.time.Instant;

public interface Auditable {

  Instant getCreatedAt();

  Instant getUpdatedAt();

  void setCreatedAt(Instant createdAt);

  void setUpdatedAt(Instant updatedAt);
}
