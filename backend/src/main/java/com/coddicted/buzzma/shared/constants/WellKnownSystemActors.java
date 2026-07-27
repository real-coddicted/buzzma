package com.coddicted.buzzma.shared.constants;

import java.util.UUID;

/** Well-known identity used as the {@code requesterId} for system-triggered actions. */
public interface WellKnownSystemActors {
  UUID SYSTEM_USER_ID = new UUID(0L, 0L);
}
