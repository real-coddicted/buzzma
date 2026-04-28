package com.coddicted.buzzma.shared.common;

import java.util.UUID;

public interface AuditLogWriter {

  void write(
      UUID actorUserId,
      String[] actorRoles,
      String action,
      String entityType,
      String entityId,
      String metadata);
}
