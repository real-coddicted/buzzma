package com.coddicted.buzzma.shared.security;

import com.coddicted.buzzma.connection.service.ConnectionService;
import com.coddicted.buzzma.identity.security.BuzzmaUserDetails;
import java.util.UUID;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Reusable {@code @PreAuthorize} check for endpoints scoped to a path-variable user id, e.g.
 * {@code @PreAuthorize("@parentshipGuard.isParentOf(#id)")}.
 */
@Component("parentshipGuard")
public class ParentshipGuard {

  private final ConnectionService connectionService;

  public ParentshipGuard(final ConnectionService connectionService) {
    this.connectionService = connectionService;
  }

  public boolean isParentOf(final UUID id) {
    return this.connectionService.isParentOf(currentUserId(), id);
  }

  private UUID currentUserId() {
    final BuzzmaUserDetails principal =
        (BuzzmaUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    return principal.getUser().getId();
  }
}
