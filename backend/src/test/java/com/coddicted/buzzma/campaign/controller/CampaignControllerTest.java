package com.coddicted.buzzma.campaign.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
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

  // --- Negative slot validation ---

  private static final String NEGATIVE_TOTAL_SLOTS_BODY =
      FileUtils.loadResourceAsString(
          "/fixtures/input/campaign/campaign-request-negative-total-slots.json");

  private static final String NEGATIVE_SLOT_OFFERED_BODY =
      FileUtils.loadResourceAsString(
          "/fixtures/input/campaign/campaign-request-negative-slot-offered.json");

  @Test
  @WithBuzzmaUser(role = UserRole.ROLE_BRAND)
  void testCreateWithNegativeTotalSlotsReturnsBadRequest() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/campaigns")
                .contentType(MediaType.APPLICATION_JSON)
                .content(NEGATIVE_TOTAL_SLOTS_BODY))
        .andExpect(status().isBadRequest());
  }

  @Test
  @WithBuzzmaUser(role = UserRole.ROLE_BRAND)
  void testCreateWithNegativeSlotOfferedReturnsBadRequest() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/campaigns")
                .contentType(MediaType.APPLICATION_JSON)
                .content(NEGATIVE_SLOT_OFFERED_BODY))
        .andExpect(status().isBadRequest());
  }

  // --- GET /api/v1/campaigns (list, paginated) ---

  @Test
  @WithBuzzmaUser(role = UserRole.ROLE_AGENCY)
  void testListReturnsPagedResult() throws Exception {
    when(campaignService.getByOwnerId(any(), any()))
        .thenReturn(new PageImpl<>(java.util.List.of(), PageRequest.of(1, 10), 25));
    when(campaignMapper.toSummaries(any())).thenReturn(java.util.List.of());

    mockMvc
        .perform(get("/api/v1/campaigns").param("page", "1").param("size", "10"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.total").value(25))
        .andExpect(jsonPath("$.page").value(1))
        .andExpect(jsonPath("$.totalPages").value(3));
  }

  @Test
  void testListUnauthenticatedReturnsUnauthorized() throws Exception {
    mockMvc.perform(get("/api/v1/campaigns")).andExpect(status().isUnauthorized());
  }

  // --- DELETE /api/v1/campaigns/{id} ---

  @Test
  @WithBuzzmaUser(role = UserRole.ROLE_BRAND)
  void testDeleteWithBrandRoleReturns204() throws Exception {
    mockMvc
        .perform(delete("/api/v1/campaigns/" + UUID.randomUUID()))
        .andExpect(status().isNoContent());
  }

  @Test
  @WithBuzzmaUser(role = UserRole.ROLE_AGENCY)
  void testDeleteWithAgencyRoleReturns204() throws Exception {
    mockMvc
        .perform(delete("/api/v1/campaigns/" + UUID.randomUUID()))
        .andExpect(status().isNoContent());
  }

  @Test
  @WithBuzzmaUser(role = UserRole.ROLE_BUYER)
  void testDeleteWithBuyerRoleReturnsForbidden() throws Exception {
    mockMvc
        .perform(delete("/api/v1/campaigns/" + UUID.randomUUID()))
        .andExpect(status().isForbidden());
  }

  @Test
  @WithBuzzmaUser(role = UserRole.ROLE_MEDIATOR)
  void testDeleteWithMediatorRoleReturnsForbidden() throws Exception {
    mockMvc
        .perform(delete("/api/v1/campaigns/" + UUID.randomUUID()))
        .andExpect(status().isForbidden());
  }

  @Test
  void testDeleteUnauthenticatedReturnsUnauthorized() throws Exception {
    mockMvc
        .perform(delete("/api/v1/campaigns/" + UUID.randomUUID()))
        .andExpect(status().isUnauthorized());
  }
}
