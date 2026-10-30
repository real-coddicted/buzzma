package com.coddicted.buzzma.identity.security;

import com.coddicted.buzzma.identity.entity.BuzzmaUser;
import com.coddicted.buzzma.identity.entity.UserRole;
import com.coddicted.buzzma.identity.persistence.UsersRepository;
import com.coddicted.buzzma.shared.security.ViewAs;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import lombok.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Serves an admin's request as another user when {@link ViewAs#HEADER} is present: the principal is
 * swapped to the subject (so {@code @CurrentUserId}, {@code @CurrentUser} and role checks all
 * resolve to that user) while the admin is kept as the actor. View-as is read-only, so only reads
 * are let through. Must run after {@link JwtAuthenticationFilter}.
 */
@Component
public class ViewAsFilter extends OncePerRequestFilter {

  private static final String AUTH_PATH_PREFIX = "/api/v1/auth/";

  private static final Set<String> READ_METHODS = Set.of("GET", "HEAD", "OPTIONS");

  /** POST endpoints that only read data (they take a filter body instead of query params). */
  private static final Set<String> READ_ONLY_POST_PATHS =
      Set.of(
          "/api/v1/campaigns/search",
          "/api/v1/campaigns/batch",
          "/api/v1/campaigns/shared",
          "/api/v1/users/batch",
          "/api/v1/claims/review",
          "/api/v1/reports/claim-review/excel");

  /** Blocked even for reads: realtime events and account-security data are never shared. */
  private static final List<String> BLOCKED_PATH_PREFIXES =
      List.of("/api/realtime/", "/api/v1/security-questions");

  private final UsersRepository usersRepository;

  public ViewAsFilter(final UsersRepository usersRepository) {
    this.usersRepository = usersRepository;
  }

  @Override
  protected boolean shouldNotFilter(@NonNull final HttpServletRequest request) {
    return request.getRequestURI().startsWith(AUTH_PATH_PREFIX);
  }

  @Override
  protected void doFilterInternal(
      @NonNull final HttpServletRequest request,
      @NonNull final HttpServletResponse response,
      @NonNull final FilterChain filterChain)
      throws ServletException, IOException {
    final String header = request.getHeader(ViewAs.HEADER);
    final Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (header == null
        || header.isBlank()
        || auth == null
        || !(auth.getPrincipal() instanceof BuzzmaUserDetails actorDetails)) {
      // Unauthenticated requests fall through so SecurityConfig answers 401.
      filterChain.doFilter(request, response);
      return;
    }

    final BuzzmaUser actor = actorDetails.getUser();
    if (actor.getRole() != UserRole.ROLE_ADMIN) {
      reject(response, HttpStatus.FORBIDDEN, "Only admins can view as another user.");
      return;
    }
    if (!isAllowedRequest(request)) {
      reject(response, HttpStatus.FORBIDDEN, "View-as mode is read-only.");
      return;
    }

    final Optional<BuzzmaUser> subject = findSubject(header);
    if (subject.isEmpty()) {
      reject(response, HttpStatus.NOT_FOUND, "User to view as was not found.");
      return;
    }
    if (subject.get().getRole() == UserRole.ROLE_ADMIN) {
      reject(response, HttpStatus.FORBIDDEN, "Cannot view as another admin.");
      return;
    }

    final BuzzmaUserDetails subjectDetails = new BuzzmaUserDetails(subject.get(), actor);
    SecurityContextHolder.getContext()
        .setAuthentication(
            new UsernamePasswordAuthenticationToken(
                subjectDetails, null, subjectDetails.getAuthorities()));
    request.setAttribute(ViewAs.ACTOR_ID_ATTRIBUTE, actor.getId());
    request.setAttribute(ViewAs.SUBJECT_ID_ATTRIBUTE, subject.get().getId());
    filterChain.doFilter(request, response);
  }

  private boolean isAllowedRequest(final HttpServletRequest request) {
    final String path = request.getRequestURI();
    if (BLOCKED_PATH_PREFIXES.stream().anyMatch(path::startsWith)) {
      return false;
    }
    return READ_METHODS.contains(request.getMethod())
        || ("POST".equals(request.getMethod()) && READ_ONLY_POST_PATHS.contains(path));
  }

  private Optional<BuzzmaUser> findSubject(final String header) {
    final UUID subjectId;
    try {
      subjectId = UUID.fromString(header.trim());
    } catch (IllegalArgumentException ex) {
      return Optional.empty();
    }
    return this.usersRepository.findById(subjectId).filter(user -> !user.getIsDeleted());
  }

  private void reject(final HttpServletResponse response, final HttpStatus status, final String msg)
      throws IOException {
    response.setStatus(status.value());
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.getWriter().write("{\"error\":\"VIEW_AS_DENIED\",\"message\":\"" + msg + "\"}");
  }
}
