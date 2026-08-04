package com.coddicted.buzzma.claim.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.coddicted.buzzma.claim.dto.ClaimReviewWorksheetDownloadDto;
import com.coddicted.buzzma.claim.dto.ClaimReviewWorksheetResponseDto;
import com.coddicted.buzzma.claim.entity.ClaimReviewWorksheet;
import com.coddicted.buzzma.claim.entity.WorksheetRowStatus;
import com.coddicted.buzzma.claim.service.ClaimReviewWorksheetService;
import com.coddicted.buzzma.identity.entity.UserRole;
import com.coddicted.buzzma.identity.persistence.UsersRepository;
import com.coddicted.buzzma.shared.security.JwtService;
import com.coddicted.buzzma.shared.security.TestSecurityConfig;
import com.coddicted.buzzma.shared.security.WithBuzzmaUser;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

@WebMvcTest(ClaimReviewController.class)
@Import(TestSecurityConfig.class)
class ClaimReviewControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private JwtService jwtService;
  @MockBean private UsersRepository usersRepository;

  @MockBean private ClaimReviewWorksheetService worksheetService;

  private ClaimReviewWorksheet sampleWorksheet;
  private MockMultipartFile sampleFile;

  @BeforeEach
  void setUp() {
    sampleWorksheet =
        ClaimReviewWorksheet.builder()
            .id(UUID.randomUUID())
            .uploadedBy(UUID.randomUUID())
            .originalFilename("review.xlsx")
            .storageKey("claim-review-worksheets/review.xlsx")
            .rowCount(5)
            .status(WorksheetRowStatus.PENDING)
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build();

    sampleFile =
        new MockMultipartFile("file", "review.xlsx", "application/octet-stream", new byte[] {1, 2});
  }

  @Test
  @WithBuzzmaUser(role = UserRole.ROLE_AGENCY)
  void listWorkbooks_withAgencyRole_returns200WithList() throws Exception {
    final ClaimReviewWorksheetResponseDto dto =
        ClaimReviewWorksheetResponseDto.builder()
            .id(sampleWorksheet.getId())
            .originalFilename("review.xlsx")
            .rowCount(5)
            .rowsProcessed(3)
            .status(WorksheetRowStatus.PENDING)
            .createdAt(sampleWorksheet.getCreatedAt())
            .build();
    when(worksheetService.listWorkbooks(any())).thenReturn(List.of(dto));

    mockMvc
        .perform(get("/api/v1/claim-review/worksheets"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].rowCount").value(5))
        .andExpect(jsonPath("$[0].rowsProcessed").value(3))
        .andExpect(jsonPath("$[0].status").value("PENDING"));
  }

  @Test
  @WithBuzzmaUser(role = UserRole.ROLE_BRAND)
  void listWorkbooks_withBrandRole_returns200() throws Exception {
    when(worksheetService.listWorkbooks(any())).thenReturn(List.of());

    mockMvc.perform(get("/api/v1/claim-review/worksheets")).andExpect(status().isOk());
  }

  @Test
  @WithBuzzmaUser(role = UserRole.ROLE_MEDIATOR)
  void listWorkbooks_withMediatorRole_returns403() throws Exception {
    mockMvc.perform(get("/api/v1/claim-review/worksheets")).andExpect(status().isForbidden());
  }

  @Test
  void listWorkbooks_unauthenticated_returns401() throws Exception {
    mockMvc.perform(get("/api/v1/claim-review/worksheets")).andExpect(status().isUnauthorized());
  }

  @Test
  @WithBuzzmaUser(role = UserRole.ROLE_AGENCY)
  void uploadWorksheet_withAgencyRole_returns201() throws Exception {
    when(worksheetService.uploadWorksheet(any(), any())).thenReturn(sampleWorksheet);

    mockMvc
        .perform(multipart("/api/v1/claim-review/worksheets").file(sampleFile))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.rowCount").value(5))
        .andExpect(jsonPath("$.status").value("PENDING"));
  }

  @Test
  @WithBuzzmaUser(role = UserRole.ROLE_BRAND)
  void uploadWorksheet_withBrandRole_returns201() throws Exception {
    when(worksheetService.uploadWorksheet(any(), any())).thenReturn(sampleWorksheet);

    mockMvc
        .perform(multipart("/api/v1/claim-review/worksheets").file(sampleFile))
        .andExpect(status().isCreated());
  }

  @Test
  @WithBuzzmaUser(role = UserRole.ROLE_MEDIATOR)
  void uploadWorksheet_withMediatorRole_returns403() throws Exception {
    mockMvc
        .perform(multipart("/api/v1/claim-review/worksheets").file(sampleFile))
        .andExpect(status().isForbidden());
  }

  @Test
  void uploadWorksheet_unauthenticated_returns401() throws Exception {
    mockMvc
        .perform(multipart("/api/v1/claim-review/worksheets").file(sampleFile))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @WithBuzzmaUser(role = UserRole.ROLE_AGENCY)
  void uploadWorksheet_fileTooLarge_returns413() throws Exception {
    when(worksheetService.uploadWorksheet(any(), any()))
        .thenThrow(new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE));

    mockMvc
        .perform(multipart("/api/v1/claim-review/worksheets").file(sampleFile))
        .andExpect(status().isPayloadTooLarge());
  }

  @Test
  @WithBuzzmaUser(role = UserRole.ROLE_AGENCY)
  void uploadWorksheet_wrongHeader_returns422() throws Exception {
    when(worksheetService.uploadWorksheet(any(), any()))
        .thenThrow(new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY));

    mockMvc
        .perform(multipart("/api/v1/claim-review/worksheets").file(sampleFile))
        .andExpect(status().isUnprocessableEntity());
  }

  @Test
  @WithBuzzmaUser(role = UserRole.ROLE_AGENCY)
  void uploadWorksheet_emptyFile_returns201WithRowCountZero() throws Exception {
    final ClaimReviewWorksheet emptyWorksheet = sampleWorksheet.toBuilder().rowCount(0).build();
    when(worksheetService.uploadWorksheet(any(), any())).thenReturn(emptyWorksheet);

    mockMvc
        .perform(multipart("/api/v1/claim-review/worksheets").file(sampleFile))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.rowCount").value(0));
  }

  @Test
  @WithBuzzmaUser(role = UserRole.ROLE_AGENCY)
  void downloadWorksheet_withAgencyRole_returns200WithFile() throws Exception {
    final UUID id = sampleWorksheet.getId();
    final byte[] fileBytes = new byte[] {1, 2, 3};
    when(worksheetService.downloadWorksheet(eq(id)))
        .thenReturn(new ClaimReviewWorksheetDownloadDto("review.xlsx", fileBytes));

    mockMvc
        .perform(get("/api/v1/claim-review/worksheets/{id}", id))
        .andExpect(status().isOk())
        .andExpect(
            content()
                .contentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
        .andExpect(header().string("Content-Disposition", "attachment; filename=\"review.xlsx\""))
        .andExpect(content().bytes(fileBytes));
  }

  @Test
  @WithBuzzmaUser(role = UserRole.ROLE_BRAND)
  void downloadWorksheet_withBrandRole_returns200() throws Exception {
    final UUID id = sampleWorksheet.getId();
    when(worksheetService.downloadWorksheet(eq(id)))
        .thenReturn(new ClaimReviewWorksheetDownloadDto("review.xlsx", new byte[] {1}));

    mockMvc.perform(get("/api/v1/claim-review/worksheets/{id}", id)).andExpect(status().isOk());
  }

  @Test
  @WithBuzzmaUser(role = UserRole.ROLE_MEDIATOR)
  void downloadWorksheet_withMediatorRole_returns403() throws Exception {
    mockMvc
        .perform(get("/api/v1/claim-review/worksheets/{id}", UUID.randomUUID()))
        .andExpect(status().isForbidden());
  }

  @Test
  void downloadWorksheet_unauthenticated_returns401() throws Exception {
    mockMvc
        .perform(get("/api/v1/claim-review/worksheets/{id}", UUID.randomUUID()))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @WithBuzzmaUser(role = UserRole.ROLE_AGENCY)
  void downloadWorksheet_notFound_returns404() throws Exception {
    final UUID id = UUID.randomUUID();
    when(worksheetService.downloadWorksheet(eq(id)))
        .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND));

    mockMvc
        .perform(get("/api/v1/claim-review/worksheets/{id}", id))
        .andExpect(status().isNotFound());
  }
}
