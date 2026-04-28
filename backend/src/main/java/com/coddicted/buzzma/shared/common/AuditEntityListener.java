package com.coddicted.buzzma.shared.common;

import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import java.time.Instant;

public class AuditEntityListener {

  @PrePersist
  public void onPrePersist(Object entity) {
    if (entity instanceof Auditable auditable) {
      Instant now = Instant.now();
      auditable.setCreatedAt(now);
      auditable.setUpdatedAt(now);
    }
  }

  @PreUpdate
  public void onPreUpdate(Object entity) {
    if (entity instanceof Auditable auditable) {
      auditable.setUpdatedAt(Instant.now());
    }
  }
}
