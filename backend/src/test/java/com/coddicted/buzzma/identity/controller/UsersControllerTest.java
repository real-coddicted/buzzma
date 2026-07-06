package com.coddicted.buzzma.identity.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.coddicted.buzzma.connection.service.ConnectionService;
import com.coddicted.buzzma.identity.dto.UserBankingDetailDto;
import com.coddicted.buzzma.identity.dto.UserSummaryDto;
import com.coddicted.buzzma.identity.entity.BuzzmaUser;
import com.coddicted.buzzma.identity.entity.UserBankingDetail;
import com.coddicted.buzzma.identity.entity.UserRole;
import com.coddicted.buzzma.identity.mapper.UserBankingDetailMapper;
import com.coddicted.buzzma.identity.mapper.UserMapper;
import com.coddicted.buzzma.identity.persistence.UsersRepository;
import com.coddicted.buzzma.identity.service.UserBankingDetailService;
import com.coddicted.buzzma.identity.service.UserService;
import com.coddicted.buzzma.shared.exception.NotFoundException;
import com.coddicted.buzzma.shared.security.JwtService;
import com.coddicted.buzzma.shared.security.OwnershipGuard;
import com.coddicted.buzzma.shared.security.ParentshipGuard;
import com.coddicted.buzzma.shared.security.TestSecurityConfig;
import com.coddicted.buzzma.shared.security.WithBuzzmaUser;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(UsersController.class)
@Import({TestSecurityConfig.class, OwnershipGuard.class, ParentshipGuard.class})
class UsersControllerTest {

  @Autowired private MockMvc mockMvc;

  // JwtAuthenticationFilter is a @Component Filter scanned by @WebMvcTest — mock its deps
  @MockBean private JwtService jwtService;
  @MockBean private UsersRepository usersRepository;

  @MockBean private UserService userService;
  @MockBean private UserMapper userMapper;
  @MockBean private UserBankingDetailService userBankingDetailService;
  @MockBean private UserBankingDetailMapper userBankingDetailMapper;
  @MockBean private ConnectionService connectionService;

  private static final UUID TARGET_USER_ID =
      UUID.fromString("44444444-4444-4444-4444-444444444444");

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
