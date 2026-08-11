package com.coddicted.buzzma.terms.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.coddicted.buzzma.identity.entity.UserRole;
import com.coddicted.buzzma.identity.persistence.UsersRepository;
import com.coddicted.buzzma.shared.security.JwtService;
import com.coddicted.buzzma.shared.security.TestSecurityConfig;
import com.coddicted.buzzma.shared.security.WithBuzzmaUser;
import com.coddicted.buzzma.terms.dto.TermsAcceptanceStatusDto;
import com.coddicted.buzzma.terms.dto.TermsDto;
import com.coddicted.buzzma.terms.service.TermsService;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(TermsController.class)
@Import(TestSecurityConfig.class)
class TermsControllerTest {

  @Autowired private MockMvc mockMvc;

  // JwtAuthenticationFilter is a @Component Filter scanned by @WebMvcTest — mock its deps
  @MockBean private JwtService jwtService;
  @MockBean private UsersRepository usersRepository;

  @MockBean private TermsService termsService;

  private static final UUID REQUESTER_ID = UUID.fromString("55555555-5555-5555-5555-555555555555");

  // --- GET /api/v1/terms ---

  @Test
  @WithBuzzmaUser(role = UserRole.ROLE_BUYER)
  void testGetReturnsContentAndVersion() throws Exception {
    when(this.termsService.getCurrent())
        .thenReturn(TermsDto.builder().content("<p>Terms</p>").version("v1.0").build());

    mockMvc
        .perform(get("/api/v1/terms"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").value("<p>Terms</p>"))
        .andExpect(jsonPath("$.version").value("v1.0"));
  }

  // --- GET /api/v1/terms/acceptance-status ---

  @Test
  @WithBuzzmaUser(role = UserRole.ROLE_BUYER, id = "55555555-5555-5555-5555-555555555555")
  void testGetAcceptanceStatusReturnsMustReaccept() throws Exception {
    when(this.termsService.getAcceptanceStatus(REQUESTER_ID))
        .thenReturn(TermsAcceptanceStatusDto.builder().mustReaccept(true).build());

    mockMvc
        .perform(get("/api/v1/terms/acceptance-status"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.mustReaccept").value(true));
  }

  @Test
  void testGetAcceptanceStatusUnauthenticatedReturnsUnauthorized() throws Exception {
    mockMvc.perform(get("/api/v1/terms/acceptance-status")).andExpect(status().isUnauthorized());
  }

  // --- POST /api/v1/terms/accept ---

  @Test
  @WithBuzzmaUser(role = UserRole.ROLE_BUYER, id = "55555555-5555-5555-5555-555555555555")
  void testAcceptRecordsAcceptanceAndReturnsNoContent() throws Exception {
    mockMvc.perform(post("/api/v1/terms/accept")).andExpect(status().isNoContent());

    verify(this.termsService).recordAcceptance(REQUESTER_ID);
  }

  @Test
  void testAcceptUnauthenticatedReturnsUnauthorized() throws Exception {
    mockMvc.perform(post("/api/v1/terms/accept")).andExpect(status().isUnauthorized());
  }
}
