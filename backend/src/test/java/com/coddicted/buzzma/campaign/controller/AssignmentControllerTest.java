package com.coddicted.buzzma.campaign.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.coddicted.buzzma.campaign.dto.AssignmentResponseDto;
import com.coddicted.buzzma.campaign.dto.AssignmentSummaryResponseDto;
import com.coddicted.buzzma.campaign.entity.CampaignAssignmentStatus;
import com.coddicted.buzzma.campaign.mapper.AssignmentMapper;
import com.coddicted.buzzma.campaign.mapper.CampaignAssignmentMapper;
import com.coddicted.buzzma.campaign.mapper.CommissionMapper;
import com.coddicted.buzzma.campaign.model.Assignment;
import com.coddicted.buzzma.campaign.processor.CampaignAssignmentProcessor;
import com.coddicted.buzzma.campaign.service.CommissionService;
import com.coddicted.buzzma.identity.service.UserService;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

class AssignmentControllerTest {

  private static final UUID REQUESTER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
  private static final UUID AGENCY_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
  private static final UUID ASSIGNMENT_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
  private static final String AGENCY_NAME = "Acme Agency";

  private CampaignAssignmentProcessor campaignAssignmentProcessor;
  private CommissionService commissionService;
  private AssignmentMapper assignmentMapper;
  private CampaignAssignmentMapper campaignAssignmentMapper;
  private CommissionMapper commissionMapper;
  private UserService userService;
  private AssignmentController controller;

  @BeforeEach
  void setUp() {
    this.campaignAssignmentProcessor = Mockito.mock(CampaignAssignmentProcessor.class);
    this.commissionService = Mockito.mock(CommissionService.class);
    this.assignmentMapper = Mockito.mock(AssignmentMapper.class);
    this.campaignAssignmentMapper = Mockito.mock(CampaignAssignmentMapper.class);
    this.commissionMapper = Mockito.mock(CommissionMapper.class);
    this.userService = Mockito.mock(UserService.class);
    this.controller =
        new AssignmentController(
            this.campaignAssignmentProcessor,
            this.commissionService,
            this.assignmentMapper,
            this.campaignAssignmentMapper,
            this.commissionMapper,
            this.userService);
  }

  @Test
  void testGetAssignmentsEnrichesItemsWithAgencyName() {
    final AssignmentSummaryResponseDto summary =
        AssignmentSummaryResponseDto.builder().id(ASSIGNMENT_ID).agencyId(AGENCY_ID).build();
    final Pageable pageable = PageRequest.of(0, 20);
    when(this.campaignAssignmentProcessor.getAssignmentSummaries(
            REQUESTER_ID, CampaignAssignmentStatus.CAMPAIGN_ASSIGNMENT_STATUS_LOCKED, pageable))
        .thenReturn(new PageImpl<>(List.of(summary), pageable, 1));
    when(this.userService.getNamesByIds(Set.of(AGENCY_ID)))
        .thenReturn(Map.of(AGENCY_ID, AGENCY_NAME));

    final var result =
        this.controller.getAssignments(
            REQUESTER_ID, CampaignAssignmentStatus.CAMPAIGN_ASSIGNMENT_STATUS_LOCKED, 0, 20);

    assertThat(result.getItems()).hasSize(1);
    assertThat(result.getItems().get(0).getAgencyName()).isEqualTo(AGENCY_NAME);
  }

  @Test
  void testGetAssignmentByIdEnrichesItemWithAgencyName() {
    final Assignment assignment = Assignment.builder().build();
    when(this.campaignAssignmentProcessor.getAssignmentById(ASSIGNMENT_ID, REQUESTER_ID))
        .thenReturn(assignment);
    final AssignmentResponseDto mappedDto =
        AssignmentResponseDto.builder().id(ASSIGNMENT_ID).agencyId(AGENCY_ID).build();
    when(this.assignmentMapper.toResponse(assignment)).thenReturn(mappedDto);
    when(this.userService.getNamesByIds(Set.of(AGENCY_ID)))
        .thenReturn(Map.of(AGENCY_ID, AGENCY_NAME));

    final AssignmentResponseDto result =
        this.controller.getAssignmentById(REQUESTER_ID, ASSIGNMENT_ID);

    assertThat(result.getAgencyName()).isEqualTo(AGENCY_NAME);
  }
}
