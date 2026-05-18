package com.coddicted.buzzma.campaign.service.impl;

import static com.coddicted.buzzma.campaign.entity.CampaignAssignmentStatus.CAMPAIGN_ASSIGNMENT_STATUS_DRAFT;
import static com.coddicted.buzzma.campaign.service.impl.Fixtures.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;

import com.coddicted.buzzma.campaign.entity.Commission;
import com.coddicted.buzzma.campaign.entity.Deal;
import com.coddicted.buzzma.campaign.model.Assignment;
import com.coddicted.buzzma.campaign.service.CampaignAssignmentService;
import com.coddicted.buzzma.campaign.service.CampaignService;
import com.coddicted.buzzma.campaign.service.CommissionService;
import com.coddicted.buzzma.campaign.service.DealService;
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
  private AssignmentServiceImpl assignmentService;

  @BeforeEach
  void setUp() {
    this.assignmentService =
        new AssignmentServiceImpl(
            this.mockCampaignService,
            this.mockCampaignAssignmentService,
            this.mockCommissionService,
            this.mockDealService);
  }

  @Test
  void testGetAssignmentsSet() {
    doReturn(List.of(ASSIGNMENT_1))
        .when(this.mockCampaignAssignmentService)
        .listAssignmentsByAssignee(ASSIGNEE_ID, CAMPAIGN_ASSIGNMENT_STATUS_DRAFT);
    doReturn(Set.of(CAMPAIGN_1))
        .when(this.mockCampaignService)
        .findCampaignsById(Set.of(CAMPAIGN_ID_1));

    final Set<Assignment> result =
        this.assignmentService.getAssignments(ASSIGNEE_ID, CAMPAIGN_ASSIGNMENT_STATUS_DRAFT);

    assertEquals(1, result.size());
    final Assignment assignment = result.iterator().next();
    assertEquals(CAMPAIGN_1, assignment.getCampaign());
    assertEquals(ASSIGNMENT_1, assignment.getCampaignAssignment());
  }

  @Test
  void testGetAssignmentsPage() {
    final PageRequest pageable = PageRequest.of(0, 10);
    doReturn(new PageImpl<>(List.of(ASSIGNMENT_1)))
        .when(this.mockCampaignAssignmentService)
        .listAssignmentsByAssignee(ASSIGNEE_ID, CAMPAIGN_ASSIGNMENT_STATUS_DRAFT, pageable);
    doReturn(Set.of(CAMPAIGN_1))
        .when(this.mockCampaignService)
        .findCampaignsById(Set.of(CAMPAIGN_ID_1));

    final Page<Assignment> result =
        this.assignmentService.getAssignments(
            ASSIGNEE_ID, CAMPAIGN_ASSIGNMENT_STATUS_DRAFT, pageable);

    assertEquals(1, result.getContent().size());
    final Assignment assignment = result.getContent().get(0);
    assertEquals(CAMPAIGN_1, assignment.getCampaign());
    assertEquals(ASSIGNMENT_1, assignment.getCampaignAssignment());
  }

  @Test
  void testPublishAssignment() {
    doReturn(CAMPAIGN_1).when(this.mockCampaignService).getById(CAMPAIGN_ID_1);
    doReturn(ASSIGNMENT_1).when(this.mockCampaignAssignmentService).getById(ASSIGNMENT_ID_1);

    final boolean result =
        this.assignmentService.publishAssignment(
            CAMPAIGN_ID_1, ASSIGNMENT_ID_1, COMMISSION_PAISE, DEAL_PRICE_PAISE, REQUESTER_ID);

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
    assertEquals(CAMPAIGN_1, savedDeal.getCampaign());
    assertEquals(DEAL_PRICE_PAISE, savedDeal.getDealPricePaise());
    assertEquals(REQUESTER_ID, savedDeal.getOwnerId());
    assertEquals(REQUESTER_ID, savedDeal.getCreatedBy());
    assertEquals(REQUESTER_ID, savedDeal.getUpdatedBy());
  }
}
