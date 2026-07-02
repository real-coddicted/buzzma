package com.coddicted.buzzma.shared.security;

import com.coddicted.buzzma.identity.security.BuzzmaUserDetails;
import java.util.UUID;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Reusable {@code @PreAuthorize} check for endpoints scoped to a path-variable user id, e.g.
 * {@code @PreAuthorize("@ownershipGuard.isOwner(#id)")}.
 */
@Component("ownershipGuard")
public class OwnershipGuard {

  public boolean isOwner(final UUID id) {
    return currentUserId().equals(id);
  }

  private UUID currentUserId() {
    final BuzzmaUserDetails principal =
        (BuzzmaUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    return principal.getUser().getId();
  }
}
