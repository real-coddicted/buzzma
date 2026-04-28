package com.coddicted.buzzma.shared.common;

import java.time.Instant;

public interface Auditable {

  Instant getCreatedAt();

  Instant getUpdatedAt();

  void setCreatedAt(Instant createdAt);

  void setUpdatedAt(Instant updatedAt);
}
