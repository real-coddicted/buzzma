package com.coddicted.buzzma.claim.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.coddicted.buzzma.campaign.service.CampaignStepResolver;
import com.coddicted.buzzma.campaign.service.DealService;
import com.coddicted.buzzma.claim.mapper.ClaimMapper;
import com.coddicted.buzzma.claim.mapper.ClaimReviewMapper;
import com.coddicted.buzzma.claim.processor.ClaimReviewProcessor;
import com.coddicted.buzzma.claim.service.ClaimAccountingService;
import com.coddicted.buzzma.claim.service.ClaimReviewService;
import com.coddicted.buzzma.claim.service.ClaimService;
import com.coddicted.buzzma.config.ConfigProvider;
import com.coddicted.buzzma.identity.entity.UserRole;
import com.coddicted.buzzma.identity.persistence.UsersRepository;
import com.coddicted.buzzma.identity.service.UserService;
import com.coddicted.buzzma.shared.security.JwtService;
import com.coddicted.buzzma.shared.security.TestSecurityConfig;
import com.coddicted.buzzma.shared.security.WithBuzzmaUser;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Authorisation slice for {@link ClaimController}. Bulk review only ever approves, so it is
 * restricted to agencies; brands verify or reject claims one at a time via {@code submitReview}.
 */
@WebMvcTest(ClaimController.class)
@Import(TestSecurityConfig.class)
class ClaimControllerAuthorizationTest {

  private static final String AGENCY_ID = "11111111-1111-1111-1111-111111111111";
  private static final UUID CLAIM_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
  private static final String BULK_REVIEW_BODY =
      """
      [{"claimId":"33333333-3333-3333-3333-333333333333",\
      "reviewerDecision":"APPROVED","amountApprovedPaise":10000}]""";

  @Autowired private MockMvc mockMvc;

  // JwtAuthenticationFilter and RequestLoggingFilter are @Component Filters scanned by
  // @WebMvcTest — mock their deps
  @MockBean private JwtService jwtService;
  @MockBean private ConfigProvider configProvider;
  @MockBean private UsersRepository usersRepository;

  @MockBean private ClaimService claimService;
  @MockBean private ClaimReviewService claimReviewService;
  @MockBean private ClaimAccountingService claimAccountingService;
  @MockBean private DealService dealService;
  @MockBean private CampaignStepResolver campaignStepResolver;
  @MockBean private ClaimMapper claimMapper;
  @MockBean private ClaimReviewMapper claimReviewMapper;
  @MockBean private ClaimReviewProcessor claimReviewProcessor;
  @MockBean private UserService userService;

  @Test
  @WithBuzzmaUser(role = UserRole.ROLE_AGENCY, id = AGENCY_ID)
  void bulkSubmitReview_withAgencyRole_returns200() throws Exception {
    when(this.claimReviewService.findClaimReviewModels(List.of(CLAIM_ID))).thenReturn(List.of());

    this.mockMvc
        .perform(
            post("/api/v1/claims/bulkSubmitReview")
                .contentType(MediaType.APPLICATION_JSON)
                .content(BULK_REVIEW_BODY))
        .andExpect(status().isOk());

    verify(this.claimReviewService)
        .bulkApproveClaimReviews(
            Map.of(CLAIM_ID, BigInteger.valueOf(10_000)), UUID.fromString(AGENCY_ID));
  }

  @Test
  @WithBuzzmaUser(role = UserRole.ROLE_BRAND)
  void bulkSubmitReview_withBrandRole_returns403() throws Exception {
    this.mockMvc
        .perform(
            post("/api/v1/claims/bulkSubmitReview")
                .contentType(MediaType.APPLICATION_JSON)
                .content(BULK_REVIEW_BODY))
        .andExpect(status().isForbidden());

    verifyNoInteractions(this.claimReviewService);
  }

  @Test
  @WithBuzzmaUser(role = UserRole.ROLE_MEDIATOR)
  void bulkSubmitReview_withMediatorRole_returns403() throws Exception {
    this.mockMvc
        .perform(
            post("/api/v1/claims/bulkSubmitReview")
                .contentType(MediaType.APPLICATION_JSON)
                .content(BULK_REVIEW_BODY))
        .andExpect(status().isForbidden());

    verifyNoInteractions(this.claimReviewService);
  }
}
