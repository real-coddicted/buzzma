package com.coddicted.buzzma.campaign.service.impl;

import static com.coddicted.buzzma.campaign.entity.CampaignAssignmentStatus.CAMPAIGN_ASSIGNMENT_STATUS_DRAFT;
import static com.coddicted.buzzma.campaign.service.impl.Fixtures.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.coddicted.buzzma.campaign.dto.AssignmentSummaryResponseDto;
import com.coddicted.buzzma.campaign.dto.AssignmentSummaryView;
import com.coddicted.buzzma.campaign.entity.Commission;
import com.coddicted.buzzma.campaign.entity.Deal;
import com.coddicted.buzzma.campaign.mapper.AssignmentMapper;
import com.coddicted.buzzma.campaign.model.Assignment;
import com.coddicted.buzzma.campaign.service.CampaignAssignmentService;
import com.coddicted.buzzma.campaign.service.CampaignService;
import com.coddicted.buzzma.campaign.service.CommissionService;
import com.coddicted.buzzma.campaign.service.DealService;
import com.coddicted.buzzma.shared.exception.BusinessRuleViolationException;
import com.coddicted.buzzma.shared.exception.ForbiddenException;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

@ExtendWith(MockitoExtension.class)
class AssignmentServiceImplTest {

  @Mock private CampaignService mockCampaignService;
  @Mock private CampaignAssignmentService mockCampaignAssignmentService;
  @Mock private CommissionService mockCommissionService;
  @Mock private DealService mockDealService;
  @Mock private AssignmentMapper mockAssignmentMapper;
  private AssignmentServiceImpl assignmentService;

  @BeforeEach
  void setUp() {
    this.assignmentService =
        new AssignmentServiceImpl(
            this.mockCampaignService,
            this.mockCampaignAssignmentService,
            this.mockCommissionService,
            this.mockDealService,
            this.mockAssignmentMapper);
  }

  @Test
  void testGetAssignmentById() {
    when(this.mockCampaignAssignmentService.getById(ASSIGNMENT_ID_1)).thenReturn(ASSIGNMENT_1);
    when(this.mockCampaignService.getById(CAMPAIGN_ID_1)).thenReturn(CAMPAIGN_1);

    final Assignment result =
        this.assignmentService.getAssignmentById(ASSIGNMENT_ID_1, ASSIGNEE_ID);

    assertEquals(CAMPAIGN_1, result.getCampaign());
    assertEquals(ASSIGNMENT_1, result.getCampaignAssignment());
  }

  @Test
  void testGetAssignmentByIdForbiddenForOtherUser() {
    when(this.mockCampaignAssignmentService.getById(ASSIGNMENT_ID_1)).thenReturn(ASSIGNMENT_1);

    assertThrows(
        ForbiddenException.class,
        () -> this.assignmentService.getAssignmentById(ASSIGNMENT_ID_1, NON_OWNER_ID));
  }

  @Test
  void testGetAssignmentsSet() {
    when(this.mockCampaignAssignmentService.listAssignmentsByAssignee(
            ASSIGNEE_ID, CAMPAIGN_ASSIGNMENT_STATUS_DRAFT))
        .thenReturn(List.of(ASSIGNMENT_1));
    when(this.mockCampaignService.findCampaignsById(Set.of(CAMPAIGN_ID_1)))
        .thenReturn(Set.of(CAMPAIGN_1));

    final Set<Assignment> result =
        this.assignmentService.getAssignments(ASSIGNEE_ID, CAMPAIGN_ASSIGNMENT_STATUS_DRAFT);

    assertEquals(1, result.size());
    final Assignment assignment = result.iterator().next();
    assertEquals(CAMPAIGN_1, assignment.getCampaign());
    assertEquals(ASSIGNMENT_1, assignment.getCampaignAssignment());
  }

  @Test
  void testGetAssignmentSummaries() {
    final PageRequest pageable = PageRequest.of(0, 10);
    final AssignmentSummaryView mockView = org.mockito.Mockito.mock(AssignmentSummaryView.class);
    final AssignmentSummaryResponseDto mockDto = AssignmentSummaryResponseDto.builder().build();
    when(this.mockCampaignAssignmentService.listAssignmentSummaries(
            ASSIGNEE_ID, CAMPAIGN_ASSIGNMENT_STATUS_DRAFT, pageable))
        .thenReturn(new PageImpl<>(List.of(mockView)));
    when(this.mockAssignmentMapper.toSummaryResponse(mockView)).thenReturn(mockDto);

    final Page<AssignmentSummaryResponseDto> result =
        this.assignmentService.getAssignmentSummaries(
            ASSIGNEE_ID, CAMPAIGN_ASSIGNMENT_STATUS_DRAFT, pageable);

    assertEquals(1, result.getContent().size());
    assertEquals(mockDto, result.getContent().get(0));
  }

  @Test
  void testPublishAssignment() {
    when(this.mockCampaignService.getById(CAMPAIGN_ID_1)).thenReturn(CAMPAIGN_AFFILIATE_ALLOWED);
    when(this.mockCampaignAssignmentService.getById(ASSIGNMENT_ID_1)).thenReturn(ASSIGNMENT_1);

    final boolean result =
        this.assignmentService.publishAssignment(
            CAMPAIGN_ID_1,
            ASSIGNMENT_ID_1,
            COMMISSION_PAISE,
            DEAL_PRICE_PAISE,
            REQUESTER_ID,
            AFFILIATE_URL);

    assertTrue(result);

    final ArgumentCaptor<Commission> commissionCaptor = ArgumentCaptor.forClass(Commission.class);
    verify(this.mockCommissionService).create(commissionCaptor.capture(), eq(REQUESTER_ID));
    final Commission savedCommission = commissionCaptor.getValue();
    assertEquals(COMMISSION_PAISE, savedCommission.getCommissionPaise());
    assertEquals(REQUESTER_ID, savedCommission.getChargedById());
    assertEquals(REQUESTER_ID, savedCommission.getCreatedBy());
    assertEquals(REQUESTER_ID, savedCommission.getUpdatedBy());

    final ArgumentCaptor<Deal> dealCaptor = ArgumentCaptor.forClass(Deal.class);
    verify(this.mockDealService).create(dealCaptor.capture());
    final Deal savedDeal = dealCaptor.getValue();
    assertEquals(CAMPAIGN_AFFILIATE_ALLOWED, savedDeal.getCampaign());
    assertEquals(DEAL_PRICE_PAISE, savedDeal.getDealPricePaise());
    assertEquals(REQUESTER_ID, savedDeal.getOwnerId());
    assertEquals(REQUESTER_ID, savedDeal.getCreatedBy());
    assertEquals(REQUESTER_ID, savedDeal.getUpdatedBy());
    assertEquals(AFFILIATE_URL, savedDeal.getAffiliateUrl());
  }

  @Test
  void testPublishAssignmentThrowsWhenAffiliateUrlNotAllowedOnCampaign() {
    when(this.mockCampaignService.getById(CAMPAIGN_ID_1)).thenReturn(CAMPAIGN_1);

    assertThrows(
        BusinessRuleViolationException.class,
        () ->
            this.assignmentService.publishAssignment(
                CAMPAIGN_ID_1,
                ASSIGNMENT_ID_1,
                COMMISSION_PAISE,
                DEAL_PRICE_PAISE,
                REQUESTER_ID,
                AFFILIATE_URL));

    verifyNoInteractions(
        this.mockCampaignAssignmentService, this.mockCommissionService, this.mockDealService);
  }

  @Test
  void testPublishAssignmentAllowsNoAffiliateUrlWhenNotAllowedOnCampaign() {
    when(this.mockCampaignService.getById(CAMPAIGN_ID_1)).thenReturn(CAMPAIGN_1);
    when(this.mockCampaignAssignmentService.getById(ASSIGNMENT_ID_1)).thenReturn(ASSIGNMENT_1);

    final boolean result =
        this.assignmentService.publishAssignment(
            CAMPAIGN_ID_1, ASSIGNMENT_ID_1, COMMISSION_PAISE, DEAL_PRICE_PAISE, REQUESTER_ID, null);

    assertTrue(result);

    final ArgumentCaptor<Deal> dealCaptor = ArgumentCaptor.forClass(Deal.class);
    verify(this.mockDealService).create(dealCaptor.capture());
    final Deal savedDeal = dealCaptor.getValue();
    assertEquals(CAMPAIGN_1, savedDeal.getCampaign());
    assertNull(savedDeal.getAffiliateUrl());
  }
}
