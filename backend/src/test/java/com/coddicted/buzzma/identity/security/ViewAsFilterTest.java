package com.coddicted.buzzma.identity.security;

import static com.coddicted.buzzma.identity.security.Fixtures.ADMIN;
import static com.coddicted.buzzma.identity.security.Fixtures.AGENCY;
import static com.coddicted.buzzma.identity.security.Fixtures.BRAND;
import static com.coddicted.buzzma.identity.security.Fixtures.BUYER;
import static com.coddicted.buzzma.identity.security.Fixtures.DELETED_BUYER;
import static com.coddicted.buzzma.identity.security.Fixtures.OTHER_ADMIN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.coddicted.buzzma.identity.entity.BuzzmaUser;
import com.coddicted.buzzma.identity.persistence.UsersRepository;
import com.coddicted.buzzma.shared.security.ViewAs;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class ViewAsFilterTest {

  @Mock private UsersRepository mockUsersRepository;

  private ViewAsFilter filter;
  private MockHttpServletResponse response;
  private MockFilterChain chain;

  @BeforeEach
  void setUp() {
    this.filter = new ViewAsFilter(this.mockUsersRepository);
    this.response = new MockHttpServletResponse();
    this.chain = new MockFilterChain();
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void requestWithoutHeaderPassesThroughUnchanged() throws Exception {
    authenticateAs(ADMIN);
    final MockHttpServletRequest request = request("GET", "/api/v1/deals/active", null);

    this.filter.doFilter(request, this.response, this.chain);

    assertThat(this.chain.getRequest()).isSameAs(request);
    assertThat(currentDetails().getUser()).isSameAs(ADMIN);
    assertThat(currentDetails().getActor()).isNull();
    assertThat(request.getAttribute(ViewAs.ACTOR_ID_ATTRIBUTE)).isNull();
    verifyNoInteractions(this.mockUsersRepository);
  }

  @Test
  void unauthenticatedRequestWithHeaderPassesThroughForSecurityToReject() throws Exception {
    final MockHttpServletRequest request =
        request("GET", "/api/v1/deals/active", BUYER.getId().toString());

    this.filter.doFilter(request, this.response, this.chain);

    assertThat(this.chain.getRequest()).isSameAs(request);
    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    verifyNoInteractions(this.mockUsersRepository);
  }

  @Test
  void adminReadIsServedAsSubjectWithAdminAsActor() throws Exception {
    authenticateAs(ADMIN);
    when(this.mockUsersRepository.findById(BUYER.getId())).thenReturn(Optional.of(BUYER));
    final MockHttpServletRequest request =
        request("GET", "/api/v1/deals/active", BUYER.getId().toString());

    this.filter.doFilter(request, this.response, this.chain);

    assertThat(this.chain.getRequest()).isSameAs(request);
    assertThat(currentDetails().getUser()).isSameAs(BUYER);
    assertThat(currentDetails().getActor()).isSameAs(ADMIN);
    assertThat(currentAuthorities()).containsExactly("ROLE_BUYER");
    assertThat(request.getAttribute(ViewAs.ACTOR_ID_ATTRIBUTE)).isEqualTo(ADMIN.getId());
    assertThat(request.getAttribute(ViewAs.SUBJECT_ID_ATTRIBUTE)).isEqualTo(BUYER.getId());
  }

  @Test
  void adminReadOnlyPostIsServedAsSubject() throws Exception {
    authenticateAs(ADMIN);
    when(this.mockUsersRepository.findById(AGENCY.getId())).thenReturn(Optional.of(AGENCY));
    final MockHttpServletRequest request =
        request("POST", "/api/v1/claims/review", AGENCY.getId().toString());

    this.filter.doFilter(request, this.response, this.chain);

    assertThat(this.chain.getRequest()).isSameAs(request);
    assertThat(currentDetails().getUser()).isSameAs(AGENCY);
  }

  @Test
  void brandSharedCampaignsListIsServedAsSubject() throws Exception {
    authenticateAs(ADMIN);
    when(this.mockUsersRepository.findById(BRAND.getId())).thenReturn(Optional.of(BRAND));
    final MockHttpServletRequest request =
        request("POST", "/api/v1/campaigns/shared", BRAND.getId().toString());

    this.filter.doFilter(request, this.response, this.chain);

    assertThat(this.chain.getRequest()).isSameAs(request);
    assertThat(currentDetails().getUser()).isSameAs(BRAND);
  }

  @Test
  void nonAdminIsRejected() throws Exception {
    authenticateAs(AGENCY);
    final MockHttpServletRequest request =
        request("GET", "/api/v1/deals/active", BUYER.getId().toString());

    this.filter.doFilter(request, this.response, this.chain);

    assertRejected(HttpStatus.FORBIDDEN, AGENCY);
  }

  @Test
  void writeIsRejected() throws Exception {
    authenticateAs(ADMIN);
    final MockHttpServletRequest request =
        request("POST", "/api/v1/claims", BUYER.getId().toString());

    this.filter.doFilter(request, this.response, this.chain);

    assertRejected(HttpStatus.FORBIDDEN, ADMIN);
  }

  @Test
  void deleteIsRejected() throws Exception {
    authenticateAs(ADMIN);
    final MockHttpServletRequest request =
        request("DELETE", "/api/v1/connections/" + UUID.randomUUID(), BUYER.getId().toString());

    this.filter.doFilter(request, this.response, this.chain);

    assertRejected(HttpStatus.FORBIDDEN, ADMIN);
  }

  @Test
  void blockedPathIsRejectedEvenForReads() throws Exception {
    authenticateAs(ADMIN);
    final MockHttpServletRequest request =
        request("GET", "/api/realtime/stream", BUYER.getId().toString());

    this.filter.doFilter(request, this.response, this.chain);

    assertRejected(HttpStatus.FORBIDDEN, ADMIN);
  }

  @Test
  void malformedSubjectIdIsNotFound() throws Exception {
    authenticateAs(ADMIN);
    final MockHttpServletRequest request = request("GET", "/api/v1/deals/active", "not-a-uuid");

    this.filter.doFilter(request, this.response, this.chain);

    assertRejected(HttpStatus.NOT_FOUND, ADMIN);
  }

  @Test
  void unknownSubjectIsNotFound() throws Exception {
    authenticateAs(ADMIN);
    when(this.mockUsersRepository.findById(BUYER.getId())).thenReturn(Optional.empty());
    final MockHttpServletRequest request =
        request("GET", "/api/v1/deals/active", BUYER.getId().toString());

    this.filter.doFilter(request, this.response, this.chain);

    assertRejected(HttpStatus.NOT_FOUND, ADMIN);
  }

  @Test
  void deletedSubjectIsNotFound() throws Exception {
    authenticateAs(ADMIN);
    when(this.mockUsersRepository.findById(DELETED_BUYER.getId()))
        .thenReturn(Optional.of(DELETED_BUYER));
    final MockHttpServletRequest request =
        request("GET", "/api/v1/deals/active", DELETED_BUYER.getId().toString());

    this.filter.doFilter(request, this.response, this.chain);

    assertRejected(HttpStatus.NOT_FOUND, ADMIN);
  }

  @Test
  void viewingAsAnotherAdminIsRejected() throws Exception {
    authenticateAs(ADMIN);
    when(this.mockUsersRepository.findById(OTHER_ADMIN.getId()))
        .thenReturn(Optional.of(OTHER_ADMIN));
    final MockHttpServletRequest request =
        request("GET", "/api/v1/deals/active", OTHER_ADMIN.getId().toString());

    this.filter.doFilter(request, this.response, this.chain);

    assertRejected(HttpStatus.FORBIDDEN, ADMIN);
  }

  @Test
  void authPathsAreNotFiltered() throws Exception {
    authenticateAs(ADMIN);
    final MockHttpServletRequest request =
        request("POST", "/api/v1/auth/refresh", BUYER.getId().toString());

    this.filter.doFilter(request, this.response, this.chain);

    assertThat(this.chain.getRequest()).isSameAs(request);
    assertThat(currentDetails().getUser()).isSameAs(ADMIN);
    verifyNoInteractions(this.mockUsersRepository);
  }

  private static MockHttpServletRequest request(
      final String method, final String uri, final String viewAsHeader) {
    final MockHttpServletRequest request = new MockHttpServletRequest(method, uri);
    if (viewAsHeader != null) {
      request.addHeader(ViewAs.HEADER, viewAsHeader);
    }
    return request;
  }

  private static void authenticateAs(final BuzzmaUser user) {
    final BuzzmaUserDetails details = new BuzzmaUserDetails(user);
    SecurityContextHolder.getContext()
        .setAuthentication(
            new UsernamePasswordAuthenticationToken(details, null, details.getAuthorities()));
  }

  private static BuzzmaUserDetails currentDetails() {
    return (BuzzmaUserDetails)
        SecurityContextHolder.getContext().getAuthentication().getPrincipal();
  }

  private static List<String> currentAuthorities() {
    final Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    return auth.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();
  }

  private void assertRejected(final HttpStatus status, final BuzzmaUser unchangedPrincipal)
      throws Exception {
    assertThat(this.response.getStatus()).isEqualTo(status.value());
    assertThat(this.response.getContentAsString()).contains("\"error\":\"VIEW_AS_DENIED\"");
    assertThat(this.chain.getRequest()).isNull();
    assertThat(currentDetails().getUser()).isSameAs(unchangedPrincipal);
  }
}
