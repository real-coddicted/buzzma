package com.coddicted.buzzma.campaign.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.coddicted.buzzma.campaign.dto.CampaignResponseDto;
import com.coddicted.buzzma.campaign.mapper.CampaignMapper;
import com.coddicted.buzzma.campaign.mapper.CampaignTypeStepMapper;
import com.coddicted.buzzma.campaign.processor.CampaignProcessor;
import com.coddicted.buzzma.campaign.service.CampaignService;
import com.coddicted.buzzma.campaign.service.CampaignTypeStepService;
import com.coddicted.buzzma.identity.entity.UserRole;
import com.coddicted.buzzma.identity.persistence.UsersRepository;
import com.coddicted.buzzma.shared.security.JwtService;
import com.coddicted.buzzma.shared.security.TestSecurityConfig;
import com.coddicted.buzzma.shared.security.WithBuzzmaUser;
import com.coddicted.buzzma.shared.util.FileUtils;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(CampaignController.class)
@Import(TestSecurityConfig.class)
class CampaignControllerTest {

  @Autowired private MockMvc mockMvc;

  // JwtAuthenticationFilter is a @Component Filter scanned by @WebMvcTest — mock its deps
  @MockBean private JwtService jwtService;
  @MockBean private UsersRepository usersRepository;

  @MockBean private CampaignService campaignService;
  @MockBean private CampaignMapper campaignMapper;
  @MockBean private CampaignTypeStepMapper campaignTypeStepMapper;
  @MockBean private CampaignProcessor campaignProcessor;
  @MockBean private CampaignTypeStepService campaignTypeStepService;

  private static final String VALID_BODY =
      FileUtils.loadResourceAsString("/fixtures/input/campaign/campaign-request.json");

  // --- POST /api/v1/campaigns (create) ---

  @Test
  @WithBuzzmaUser(role = UserRole.ROLE_BRAND)
  void testCreateWithBrandRoleReturns201() throws Exception {
    when(campaignProcessor.create(any(), any())).thenReturn(CampaignResponseDto.builder().build());

    mockMvc
        .perform(
            post("/api/v1/campaigns").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
        .andExpect(status().isCreated());
  }

  @Test
  @WithBuzzmaUser(role = UserRole.ROLE_AGENCY)
  void testCreateWithAgencyRoleReturns201() throws Exception {
    when(campaignProcessor.create(any(), any())).thenReturn(CampaignResponseDto.builder().build());

    mockMvc
        .perform(
            post("/api/v1/campaigns").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
        .andExpect(status().isCreated());
  }

  @Test
  @WithBuzzmaUser(role = UserRole.ROLE_BUYER)
  void testCreateWithBuyerRoleReturnsForbidden() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/campaigns").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
        .andExpect(status().isForbidden());
  }

  @Test
  @WithBuzzmaUser(role = UserRole.ROLE_MEDIATOR)
  void testCreateWithMediatorRoleReturnsForbidden() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/campaigns").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
        .andExpect(status().isForbidden());
  }

  @Test
  void testCreateUnauthenticatedReturnsUnauthorized() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/campaigns").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
        .andExpect(status().isUnauthorized());
  }

  // --- PATCH /api/v1/campaigns/{id} (update) ---

  @Test
  @WithBuzzmaUser(role = UserRole.ROLE_BRAND)
  void testUpdateWithBrandRoleReturns200() throws Exception {
    when(campaignProcessor.updateCampaign(any(), any(), any()))
        .thenReturn(CampaignResponseDto.builder().build());

    mockMvc
        .perform(
            patch("/api/v1/campaigns/" + UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_BODY))
        .andExpect(status().isOk());
  }

  @Test
  @WithBuzzmaUser(role = UserRole.ROLE_AGENCY)
  void testUpdateWithAgencyRoleReturns200() throws Exception {
    when(campaignProcessor.updateCampaign(any(), any(), any()))
        .thenReturn(CampaignResponseDto.builder().build());

    mockMvc
        .perform(
            patch("/api/v1/campaigns/" + UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_BODY))
        .andExpect(status().isOk());
  }

  @Test
  @WithBuzzmaUser(role = UserRole.ROLE_BUYER)
  void testUpdateWithBuyerRoleReturnsForbidden() throws Exception {
    mockMvc
        .perform(
            patch("/api/v1/campaigns/" + UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_BODY))
        .andExpect(status().isForbidden());
  }

  @Test
  void testUpdateUnauthenticatedReturnsUnauthorized() throws Exception {
    mockMvc
        .perform(
            patch("/api/v1/campaigns/" + UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_BODY))
        .andExpect(status().isUnauthorized());
  }
}
