package com.coddicted.buzzma.shared.security;

import com.coddicted.buzzma.identity.entity.BuzzmaUser;
import com.coddicted.buzzma.identity.security.BuzzmaUserDetails;
import java.util.UUID;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

/**
 * Provides a {@link BuzzmaUserDetails} principal so that {@code @CurrentUserId}'s SpEL expression
 * {@code "user.id"} resolves correctly — {@code @WithMockUser} injects a plain Spring {@code User}
 * which does not have a {@code user} property and causes a {@code SpelEvaluationException}.
 */
public class WithBuzzmaUserSecurityContextFactory
    implements WithSecurityContextFactory<WithBuzzmaUser> {

  @Override
  public SecurityContext createSecurityContext(final WithBuzzmaUser annotation) {
    final UUID id =
        annotation.id().isBlank() ? UUID.randomUUID() : UUID.fromString(annotation.id());
    final BuzzmaUser user =
        BuzzmaUser.builder()
            .id(id)
            .role(annotation.role())
            .name("Test User")
            .mobile("9999999999")
            .isDeleted(false)
            .build();
    final BuzzmaUserDetails details = new BuzzmaUserDetails(user);
    final UsernamePasswordAuthenticationToken auth =
        new UsernamePasswordAuthenticationToken(details, null, details.getAuthorities());
    final SecurityContext context = SecurityContextHolder.createEmptyContext();
    context.setAuthentication(auth);
    return context;
  }
}
