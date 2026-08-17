package com.coddicted.buzzma.identity.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.coddicted.buzzma.config.ConfigProvider;
import com.coddicted.buzzma.connection.service.ConnectionService;
import com.coddicted.buzzma.identity.dto.UserBankingDetailDto;
import com.coddicted.buzzma.identity.dto.UserSummaryDto;
import com.coddicted.buzzma.identity.entity.BuzzmaUser;
import com.coddicted.buzzma.identity.entity.UpiDetails;
import com.coddicted.buzzma.identity.entity.UserBankingDetail;
import com.coddicted.buzzma.identity.entity.UserRole;
import com.coddicted.buzzma.identity.mapper.UserBankingDetailMapper;
import com.coddicted.buzzma.identity.mapper.UserMapper;
import com.coddicted.buzzma.identity.persistence.UsersRepository;
import com.coddicted.buzzma.identity.service.EmailVerificationService;
import com.coddicted.buzzma.identity.service.UserBankingDetailService;
import com.coddicted.buzzma.identity.service.UserService;
import com.coddicted.buzzma.shared.exception.NotFoundException;
import com.coddicted.buzzma.shared.security.JwtService;
import com.coddicted.buzzma.shared.security.OwnershipGuard;
import com.coddicted.buzzma.shared.security.ParentshipGuard;
import com.coddicted.buzzma.shared.security.TestSecurityConfig;
import com.coddicted.buzzma.shared.security.WithBuzzmaUser;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(UsersController.class)
@Import({TestSecurityConfig.class, OwnershipGuard.class, ParentshipGuard.class})
class UsersControllerTest {

  @Autowired private MockMvc mockMvc;

  // JwtAuthenticationFilter and RequestLoggingFilter are @Component Filters scanned by
  // @WebMvcTest — mock their deps
  @MockBean private JwtService jwtService;
  @MockBean private ConfigProvider configProvider;
  @MockBean private UsersRepository usersRepository;

  @MockBean private UserService userService;
  @MockBean private UserMapper userMapper;
  @MockBean private UserBankingDetailService userBankingDetailService;
  @MockBean private UserBankingDetailMapper userBankingDetailMapper;
  @MockBean private ConnectionService connectionService;
  @MockBean private EmailVerificationService emailVerificationService;

  private static final UUID TARGET_USER_ID =
      UUID.fromString("44444444-4444-4444-4444-444444444444");

  private static final String CALLER_ID_STR = "55555555-5555-5555-5555-555555555555";
  private static final UUID CALLER_ID = UUID.fromString(CALLER_ID_STR);

  // --- POST /api/v1/users/me ---

  @Test
  @WithBuzzmaUser(role = UserRole.ROLE_BUYER, id = "44444444-4444-4444-4444-444444444444")
  void testUpdateProfileWithValidEmailReturnsOk() throws Exception {
    final BuzzmaUser user =
        BuzzmaUser.builder().id(TARGET_USER_ID).email("new@example.com").build();
    when(userService.updateProfile(eq("new@example.com"), eq(TARGET_USER_ID))).thenReturn(user);
    when(userMapper.toUserSummaryDto(user))
        .thenReturn(UserSummaryDto.builder().id(TARGET_USER_ID).email("new@example.com").build());

    mockMvc
        .perform(
            post("/api/v1/users/me")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\": \"new@example.com\"}"))
        .andExpect(status().isOk());

    verify(userService).updateProfile("new@example.com", TARGET_USER_ID);
  }

  @Test
  @WithBuzzmaUser(role = UserRole.ROLE_BUYER)
  void testUpdateProfileWithMissingEmailReturnsBadRequest() throws Exception {
    mockMvc
        .perform(post("/api/v1/users/me").contentType(MediaType.APPLICATION_JSON).content("{}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  @WithBuzzmaUser(role = UserRole.ROLE_BUYER)
  void testUpdateProfileWithBlankEmailReturnsBadRequest() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/users/me")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\": \"\"}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  @WithBuzzmaUser(role = UserRole.ROLE_BUYER)
  void testUpdateProfileWithMalformedEmailReturnsBadRequest() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/users/me")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\": \"not-an-email\"}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void testUpdateProfileUnauthenticatedReturnsUnauthorized() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/users/me")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\": \"new@example.com\"}"))
        .andExpect(status().isUnauthorized());
  }

  // --- POST /api/v1/users/batch ---

  @Test
  @WithBuzzmaUser(role = UserRole.ROLE_AGENCY, id = CALLER_ID_STR)
  void testGetByIdsReturnsBriefInfoOnlyForConnectedUsers() throws Exception {
    final UUID otherId = UUID.fromString("66666666-6666-6666-6666-666666666666");

    final BuzzmaUser target =
        BuzzmaUser.builder().id(TARGET_USER_ID).name("Target").role(UserRole.ROLE_MEDIATOR).build();
    when(userService.getConnectedByIds(
            argThat(ids -> new HashSet<>(ids).equals(Set.of(TARGET_USER_ID, otherId))),
            eq(CALLER_ID)))
        .thenReturn(List.of(target));
    final UserBankingDetail bankingDetail =
        UserBankingDetail.builder()
            .userId(TARGET_USER_ID)
            .upiDetails(UpiDetails.builder().upiId("target@upi").build())
            .build();
    when(userBankingDetailService.getByUserIds(Set.of(TARGET_USER_ID)))
        .thenReturn(Map.of(TARGET_USER_ID, bankingDetail));

    mockMvc
        .perform(
            post("/api/v1/users/batch")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"ids\": [\"" + TARGET_USER_ID + "\", \"" + otherId + "\"]}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$[0].id").value(TARGET_USER_ID.toString()));
  }

  @Test
  void testGetByIdsUnauthenticatedReturnsUnauthorized() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/users/batch")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"ids\": [\"" + TARGET_USER_ID + "\"]}"))
        .andExpect(status().isUnauthorized());
  }

  // --- GET /api/v1/users/{id} ---

  @Test
  @WithBuzzmaUser(role = UserRole.ROLE_ADMIN)
  void testGetByIdAsAdminReturnsUserSummary() throws Exception {
    final BuzzmaUser user = BuzzmaUser.builder().id(TARGET_USER_ID).build();
    when(userService.getById(TARGET_USER_ID)).thenReturn(user);
    when(userMapper.toUserSummaryDto(user))
        .thenReturn(UserSummaryDto.builder().id(TARGET_USER_ID).build());

    mockMvc.perform(get("/api/v1/users/" + TARGET_USER_ID)).andExpect(status().isOk());
  }

  @Test
  @WithBuzzmaUser(role = UserRole.ROLE_AGENCY)
  void testGetByIdAsParentAgencyReturnsUserSummary() throws Exception {
    when(connectionService.isParentOf(any(), any())).thenReturn(true);
    final BuzzmaUser user = BuzzmaUser.builder().id(TARGET_USER_ID).build();
    when(userService.getById(TARGET_USER_ID)).thenReturn(user);
    when(userMapper.toUserSummaryDto(user))
        .thenReturn(UserSummaryDto.builder().id(TARGET_USER_ID).build());

    mockMvc.perform(get("/api/v1/users/" + TARGET_USER_ID)).andExpect(status().isOk());
  }

  @Test
  @WithBuzzmaUser(role = UserRole.ROLE_AGENCY)
  void testGetByIdAsNonParentAgencyReturnsForbidden() throws Exception {
    when(connectionService.isParentOf(any(), any())).thenReturn(false);

    mockMvc.perform(get("/api/v1/users/" + TARGET_USER_ID)).andExpect(status().isForbidden());
  }

  @Test
  @WithBuzzmaUser(role = UserRole.ROLE_BRAND)
  void testGetByIdAsParentBrandReturnsUserSummary() throws Exception {
    when(connectionService.isParentOf(any(), any())).thenReturn(true);
    final BuzzmaUser user = BuzzmaUser.builder().id(TARGET_USER_ID).build();
    when(userService.getById(TARGET_USER_ID)).thenReturn(user);
    when(userMapper.toUserSummaryDto(user))
        .thenReturn(UserSummaryDto.builder().id(TARGET_USER_ID).build());

    mockMvc.perform(get("/api/v1/users/" + TARGET_USER_ID)).andExpect(status().isOk());
  }

  @Test
  @WithBuzzmaUser(role = UserRole.ROLE_BRAND)
  void testGetByIdAsNonParentBrandReturnsForbidden() throws Exception {
    when(connectionService.isParentOf(any(), any())).thenReturn(false);

    mockMvc.perform(get("/api/v1/users/" + TARGET_USER_ID)).andExpect(status().isForbidden());
  }

  @Test
  @WithBuzzmaUser(role = UserRole.ROLE_BUYER)
  void testGetByIdAsBuyerReturnsForbidden() throws Exception {
    mockMvc.perform(get("/api/v1/users/" + TARGET_USER_ID)).andExpect(status().isForbidden());
  }

  @Test
  void testGetByIdUnauthenticatedReturnsUnauthorized() throws Exception {
    mockMvc.perform(get("/api/v1/users/" + TARGET_USER_ID)).andExpect(status().isUnauthorized());
  }

  // --- GET /api/v1/users/{id}/banking ---

  @Test
  @WithBuzzmaUser(role = UserRole.ROLE_BUYER, id = "44444444-4444-4444-4444-444444444444")
  void testGetBankingWhenSelfReturnsOk() throws Exception {
    final UserBankingDetail detail = UserBankingDetail.builder().userId(TARGET_USER_ID).build();
    when(userBankingDetailService.getByUserId(TARGET_USER_ID)).thenReturn(detail);
    when(userBankingDetailMapper.toDto(detail)).thenReturn(UserBankingDetailDto.builder().build());

    mockMvc.perform(get("/api/v1/users/" + TARGET_USER_ID + "/banking")).andExpect(status().isOk());
  }

  @Test
  @WithBuzzmaUser(role = UserRole.ROLE_BUYER)
  void testGetBankingWhenParentOfTargetReturnsOk() throws Exception {
    when(connectionService.isParentOf(any(), any())).thenReturn(true);
    final UserBankingDetail detail = UserBankingDetail.builder().userId(TARGET_USER_ID).build();
    when(userBankingDetailService.getByUserId(TARGET_USER_ID)).thenReturn(detail);
    when(userBankingDetailMapper.toDto(detail)).thenReturn(UserBankingDetailDto.builder().build());

    mockMvc.perform(get("/api/v1/users/" + TARGET_USER_ID + "/banking")).andExpect(status().isOk());
  }

  @Test
  @WithBuzzmaUser(role = UserRole.ROLE_BUYER)
  void testGetBankingWhenNotParentOfTargetReturnsForbidden() throws Exception {
    when(connectionService.isParentOf(any(), any())).thenReturn(false);

    mockMvc
        .perform(get("/api/v1/users/" + TARGET_USER_ID + "/banking"))
        .andExpect(status().isForbidden());
  }

  @Test
  @WithBuzzmaUser(role = UserRole.ROLE_BUYER)
  void testGetBankingWhenParentOfTargetButNoBankingDetailReturnsNotFound() throws Exception {
    when(connectionService.isParentOf(any(), any())).thenReturn(true);
    when(userBankingDetailService.getByUserId(TARGET_USER_ID))
        .thenThrow(new NotFoundException("Banking detail not found for user: " + TARGET_USER_ID));

    mockMvc
        .perform(get("/api/v1/users/" + TARGET_USER_ID + "/banking"))
        .andExpect(status().isNotFound());
  }

  @Test
  void testGetBankingUnauthenticatedReturnsUnauthorized() throws Exception {
    mockMvc
        .perform(get("/api/v1/users/" + TARGET_USER_ID + "/banking"))
        .andExpect(status().isUnauthorized());
  }
}
