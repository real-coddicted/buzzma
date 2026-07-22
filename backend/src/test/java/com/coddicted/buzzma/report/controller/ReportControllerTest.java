package com.coddicted.buzzma.report.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.coddicted.buzzma.identity.entity.UserRole;
import com.coddicted.buzzma.identity.persistence.UsersRepository;
import com.coddicted.buzzma.report.service.ReportService;
import com.coddicted.buzzma.shared.constants.WellKnownReports;
import com.coddicted.buzzma.shared.security.JwtService;
import com.coddicted.buzzma.shared.security.TestSecurityConfig;
import com.coddicted.buzzma.shared.security.WithBuzzmaUser;
import com.coddicted.buzzma.shared.util.DateTimeUtils;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ReportController.class)
@Import(TestSecurityConfig.class)
class ReportControllerTest {

  @Autowired private MockMvc mockMvc;

  // JwtAuthenticationFilter is a @Component Filter scanned by @WebMvcTest — mock its deps
  @MockBean private JwtService jwtService;
  @MockBean private UsersRepository usersRepository;

  @MockBean private ReportService reportService;

  @Test
  @WithBuzzmaUser(role = UserRole.ROLE_AGENCY)
  void testClaimReviewReportWithAgencyRoleReturnsXlsx() throws Exception {
    when(reportService.generateClaimReviewReport(any(), any())).thenReturn(new byte[] {1, 2, 3});

    mockMvc
        .perform(post("/api/v1/reports/claim-review/excel").contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(
            header()
                .string(
                    "Content-Type",
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
        .andExpect(
            header()
                .string(
                    "Content-Disposition",
                    "attachment; filename=\""
                        + WellKnownReports.CLAIM_REVIEW_FILE_BASE_NAME
                        + "_"
                        + DateTimeUtils.DATE_FORMAT.format(LocalDate.now())
                        + ".xlsx\""));
  }

  @Test
  @WithBuzzmaUser(role = UserRole.ROLE_MEDIATOR)
  void testClaimReviewReportWithMediatorRoleReturnsXlsx() throws Exception {
    when(reportService.generateClaimReviewReport(any(), any())).thenReturn(new byte[] {1, 2, 3});

    mockMvc
        .perform(post("/api/v1/reports/claim-review/excel").contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());
  }

  @Test
  @WithBuzzmaUser(role = UserRole.ROLE_BUYER)
  void testClaimReviewReportWithBuyerRoleReturnsForbidden() throws Exception {
    mockMvc
        .perform(post("/api/v1/reports/claim-review/excel").contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden());
  }

  @Test
  void testClaimReviewReportUnauthenticatedReturnsUnauthorized() throws Exception {
    mockMvc
        .perform(post("/api/v1/reports/claim-review/excel").contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isUnauthorized());
  }
}
