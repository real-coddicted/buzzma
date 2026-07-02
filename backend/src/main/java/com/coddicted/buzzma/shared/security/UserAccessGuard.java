package com.coddicted.buzzma.shared.security;

import com.coddicted.buzzma.connection.service.ConnectionService;
import com.coddicted.buzzma.identity.security.BuzzmaUserDetails;
import java.util.UUID;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Reusable {@code @PreAuthorize} checks for endpoints scoped to a path-variable user id, e.g.
 * {@code @PreAuthorize("isOwner(#id) or isConnected(#id)")}.
 */
@Component("userAccessGuard")
public class UserAccessGuard {

  private final ConnectionService connectionService;

  public UserAccessGuard(final ConnectionService connectionService) {
    this.connectionService = connectionService;
  }

  public boolean isOwner(final UUID id) {
    return currentUserId().equals(id);
  }

  public boolean isConnected(final UUID id) {
    return this.connectionService.isConnected(currentUserId(), id);
  }

  private UUID currentUserId() {
    final BuzzmaUserDetails principal =
        (BuzzmaUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    return principal.getUser().getId();
  }
}
