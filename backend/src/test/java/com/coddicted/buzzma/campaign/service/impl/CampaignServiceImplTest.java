package com.coddicted.buzzma.campaign.service.impl;

import static com.coddicted.buzzma.campaign.entity.CampaignAction.CAMPAIGN_ACTION_CLOSE;
import static com.coddicted.buzzma.campaign.entity.CampaignAction.CAMPAIGN_ACTION_COMPLETE;
import static com.coddicted.buzzma.campaign.entity.CampaignAction.CAMPAIGN_ACTION_PAUSE;
import static com.coddicted.buzzma.campaign.entity.CampaignAction.CAMPAIGN_ACTION_PUBLISH;
import static com.coddicted.buzzma.campaign.entity.CampaignAction.CAMPAIGN_ACTION_RESUME;
import static com.coddicted.buzzma.campaign.entity.CampaignAssignmentStatus.CAMPAIGN_ASSIGNMENT_STATUS_LOCKED;
import static com.coddicted.buzzma.campaign.entity.CampaignAssignmentStatus.CAMPAIGN_ASSIGNMENT_STATUS_PUBLISHED;
import static com.coddicted.buzzma.campaign.entity.CampaignStatus.CAMPAIGN_STATUS_ACTIVE;
import static com.coddicted.buzzma.campaign.entity.CampaignStatus.CAMPAIGN_STATUS_CLOSED;
import static com.coddicted.buzzma.campaign.entity.CampaignStatus.CAMPAIGN_STATUS_COMPLETED;
import static com.coddicted.buzzma.campaign.entity.CampaignStatus.CAMPAIGN_STATUS_DRAFT;
import static com.coddicted.buzzma.campaign.entity.CampaignStatus.CAMPAIGN_STATUS_PAUSED;
import static com.coddicted.buzzma.campaign.entity.CampaignType.CAMPAIGN_TYPE_REVIEW;
import static com.coddicted.buzzma.campaign.service.impl.Fixtures.*;
import static com.coddicted.buzzma.shared.enums.Platform.PLATFORM_AMAZON;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.coddicted.buzzma.campaign.entity.Campaign;
import com.coddicted.buzzma.campaign.entity.CampaignAssignment;
import com.coddicted.buzzma.campaign.model.CampaignSearchCriteria;
import com.coddicted.buzzma.campaign.model.CampaignSummary;
import com.coddicted.buzzma.campaign.notification.CampaignEventPublisher;
import com.coddicted.buzzma.campaign.persistence.CampaignAssignmentRepository;
import com.coddicted.buzzma.campaign.persistence.CampaignRepository;
import com.coddicted.buzzma.campaign.persistence.CampaignSlotRepository;
import com.coddicted.buzzma.campaign.service.CampaignAssignmentService;
import com.coddicted.buzzma.campaign.service.CampaignStateMachine;
import com.coddicted.buzzma.shared.constants.WellKnownSequences;
import com.coddicted.buzzma.shared.exception.BusinessRuleViolationException;
import com.coddicted.buzzma.shared.exception.ForbiddenException;
import com.coddicted.buzzma.shared.exception.NotFoundException;
import com.coddicted.buzzma.shared.service.CodeGenerationService;
import java.util.List;
import java.util.Optional;
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
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class CampaignServiceImplTest {

  @Mock private CampaignRepository mockCampaignRepository;
  @Mock private CampaignAssignmentRepository mockCampaignAssignmentRepository;
  @Mock private CampaignAssignmentService mockCampaignAssignmentService;
  @Mock private CampaignSlotRepository mockCampaignSlotRepository;
  @Mock private CampaignStateMachine mockStateMachine;
  @Mock private CampaignEventPublisher mockCampaignEventPublisher;
  @Mock private CodeGenerationService mockCodeGenerationService;

  private CampaignServiceImpl campaignService;

  @BeforeEach
  void setUp() {
    this.campaignService =
        new CampaignServiceImpl(
            this.mockCampaignRepository,
            this.mockCampaignAssignmentRepository,
            this.mockCampaignAssignmentService,
            this.mockCampaignSlotRepository,
            this.mockStateMachine,
            this.mockCampaignEventPublisher,
            this.mockCodeGenerationService);
  }

  @Test
  void testGetByIdWhenFound() {
    when(this.mockCampaignRepository.findById(CAMPAIGN_ID_1)).thenReturn(Optional.of(CAMPAIGN_1));

    final Campaign result = this.campaignService.getById(CAMPAIGN_ID_1);

    assertEquals(CAMPAIGN_1, result);
  }

  @Test
  void testGetByIdWhenNotFound() {
    when(this.mockCampaignRepository.findById(CAMPAIGN_ID_1)).thenReturn(Optional.empty());

    final NotFoundException ex =
        assertThrows(NotFoundException.class, () -> this.campaignService.getById(CAMPAIGN_ID_1));
    assertEquals("Campaign not found: " + CAMPAIGN_ID_1, ex.getMessage());
  }

  @Test
  void testCreate() {
    when(this.mockCodeGenerationService.generateCodeFromSequence(WellKnownSequences.CAMPAIGN))
        .thenReturn(GENERATED_CODE);
    final ArgumentCaptor<Campaign> captor = ArgumentCaptor.forClass(Campaign.class);
    when(this.mockCampaignRepository.save(captor.capture())).thenReturn(CAMPAIGN_1);

    this.campaignService.create(CAMPAIGN_1);

    final Campaign saved = captor.getValue();
    assertEquals(GENERATED_CODE, saved.getCode());
    assertEquals(CAMPAIGN_ID_1, saved.getId());
  }

  @Test
  void testUpdate() {
    when(this.mockCampaignRepository.save(CAMPAIGN_1)).thenReturn(CAMPAIGN_1);

    final Campaign result = this.campaignService.update(CAMPAIGN_1);

    assertEquals(CAMPAIGN_1, result);
  }

  @Test
  void testDelete() {
    when(this.mockCampaignRepository.deleteDraftCampaign(OWNER_ID, CAMPAIGN_ID_1)).thenReturn(1);
    when(this.mockCampaignRepository.findById(CAMPAIGN_ID_1)).thenReturn(Optional.of(CAMPAIGN_1));

    final Campaign result = this.campaignService.delete(CAMPAIGN_ID_1, OWNER_ID);

    assertEquals(CAMPAIGN_1, result);
  }

  @Test
  void testDeleteWhenNotFound() {
    when(this.mockCampaignRepository.deleteDraftCampaign(OWNER_ID, CAMPAIGN_ID_1)).thenReturn(0);

    assertThrows(
        BusinessRuleViolationException.class,
        () -> this.campaignService.delete(CAMPAIGN_ID_1, OWNER_ID));
  }

  @Test
  void testDeleteWhenNotOwner() {
    when(this.mockCampaignRepository.deleteDraftCampaign(NON_OWNER_ID, CAMPAIGN_ID_1))
        .thenReturn(0);

    assertThrows(
        BusinessRuleViolationException.class,
        () -> this.campaignService.delete(CAMPAIGN_ID_1, NON_OWNER_ID));
  }

  @Test
  void testDeleteWhenNotDraft() {
    when(this.mockCampaignRepository.deleteDraftCampaign(OWNER_ID, CAMPAIGN_ID_2)).thenReturn(0);

    assertThrows(
        BusinessRuleViolationException.class,
        () -> this.campaignService.delete(CAMPAIGN_ID_2, OWNER_ID));
  }

  @Test
  void testActionPublishSuccess() {
    when(this.mockCampaignRepository.findById(CAMPAIGN_ID_1)).thenReturn(Optional.of(CAMPAIGN_1));
    when(this.mockCampaignAssignmentRepository.findByCampaignId(CAMPAIGN_ID_1))
        .thenReturn(List.of(ASSIGNMENT_1));
    when(this.mockCampaignRepository.save(CAMPAIGN_1)).thenReturn(CAMPAIGN_1);

    this.campaignService.action(CAMPAIGN_ID_1, CAMPAIGN_ACTION_PUBLISH, OWNER_ID);

    verify(this.mockStateMachine).transition(CAMPAIGN_1, CAMPAIGN_STATUS_ACTIVE);
    verify(this.mockCampaignAssignmentRepository).saveAll(List.of(ASSIGNMENT_1));
    assertEquals(CAMPAIGN_ASSIGNMENT_STATUS_LOCKED, ASSIGNMENT_1.getStatus());
    verify(this.mockCampaignRepository).save(CAMPAIGN_1);
    verify(this.mockCampaignEventPublisher)
        .publishCampaignLaunchedEvent(CAMPAIGN_1, List.of(ASSIGNEE_ID));
  }

  @Test
  void testActionPublishWhenNotOwner() {
    when(this.mockCampaignRepository.findById(CAMPAIGN_ID_1)).thenReturn(Optional.of(CAMPAIGN_1));

    final ForbiddenException ex =
        assertThrows(
            ForbiddenException.class,
            () ->
                this.campaignService.action(CAMPAIGN_ID_1, CAMPAIGN_ACTION_PUBLISH, NON_OWNER_ID));
    assertEquals(
        "Only the campaign owner can perform this action on the campaign", ex.getMessage());
  }

  @Test
  void testActionPublishWhenNoAssignments() {
    when(this.mockCampaignRepository.findById(CAMPAIGN_ID_1)).thenReturn(Optional.of(CAMPAIGN_1));
    when(this.mockCampaignAssignmentRepository.findByCampaignId(CAMPAIGN_ID_1))
        .thenReturn(List.of());

    final BusinessRuleViolationException ex =
        assertThrows(
            BusinessRuleViolationException.class,
            () -> this.campaignService.action(CAMPAIGN_ID_1, CAMPAIGN_ACTION_PUBLISH, OWNER_ID));
    assertEquals("Campaign must have at least one assignment before publishing", ex.getMessage());
  }

  @Test
  void testActionPause() {
    when(this.mockCampaignRepository.findById(CAMPAIGN_ID_2)).thenReturn(Optional.of(CAMPAIGN_2));
    when(this.mockCampaignRepository.save(CAMPAIGN_2)).thenReturn(CAMPAIGN_2);
    when(this.mockCampaignAssignmentRepository.findByCampaignId(CAMPAIGN_ID_2))
        .thenReturn(List.of(ASSIGNMENT_1));

    this.campaignService.action(CAMPAIGN_ID_2, CAMPAIGN_ACTION_PAUSE, OWNER_ID);

    verify(this.mockStateMachine).transition(CAMPAIGN_2, CAMPAIGN_STATUS_PAUSED);
    verify(this.mockCampaignRepository).save(CAMPAIGN_2);
    verify(this.mockCampaignEventPublisher)
        .publishCampaignPausedEvent(CAMPAIGN_2, List.of(ASSIGNEE_ID));
  }

  @Test
  void testActionPauseWhenNotOwner() {
    when(this.mockCampaignRepository.findById(CAMPAIGN_ID_2)).thenReturn(Optional.of(CAMPAIGN_2));

    final ForbiddenException ex =
        assertThrows(
            ForbiddenException.class,
            () -> this.campaignService.action(CAMPAIGN_ID_2, CAMPAIGN_ACTION_PAUSE, NON_OWNER_ID));
    assertEquals(
        "Only the campaign owner can perform this action on the campaign", ex.getMessage());
    verifyNoInteractions(this.mockStateMachine);
  }

  @Test
  void testActionResume() {
    when(this.mockCampaignRepository.findById(CAMPAIGN_ID_3)).thenReturn(Optional.of(CAMPAIGN_3));
    when(this.mockCampaignRepository.save(CAMPAIGN_3)).thenReturn(CAMPAIGN_3);
    when(this.mockCampaignAssignmentRepository.findByCampaignId(CAMPAIGN_ID_3))
        .thenReturn(List.of(ASSIGNMENT_1));

    this.campaignService.action(CAMPAIGN_ID_3, CAMPAIGN_ACTION_RESUME, OWNER_ID);

    verify(this.mockStateMachine).transition(CAMPAIGN_3, CAMPAIGN_STATUS_ACTIVE);
    verify(this.mockCampaignRepository).save(CAMPAIGN_3);
    verify(this.mockCampaignEventPublisher)
        .publishCampaignResumedEvent(CAMPAIGN_3, List.of(ASSIGNEE_ID));
  }

  @Test
  void testActionResumeWhenNotOwner() {
    when(this.mockCampaignRepository.findById(CAMPAIGN_ID_3)).thenReturn(Optional.of(CAMPAIGN_3));

    final ForbiddenException ex =
        assertThrows(
            ForbiddenException.class,
            () -> this.campaignService.action(CAMPAIGN_ID_3, CAMPAIGN_ACTION_RESUME, NON_OWNER_ID));
    assertEquals(
        "Only the campaign owner can perform this action on the campaign", ex.getMessage());
    verifyNoInteractions(this.mockStateMachine);
  }

  @Test
  void testActionClose() {
    when(this.mockCampaignRepository.findById(CAMPAIGN_ID_2)).thenReturn(Optional.of(CAMPAIGN_2));
    when(this.mockCampaignRepository.save(CAMPAIGN_2)).thenReturn(CAMPAIGN_2);
    when(this.mockCampaignAssignmentRepository.findByCampaignId(CAMPAIGN_ID_2))
        .thenReturn(List.of(ASSIGNMENT_1));

    this.campaignService.action(CAMPAIGN_ID_2, CAMPAIGN_ACTION_CLOSE, OWNER_ID);

    verify(this.mockStateMachine).transition(CAMPAIGN_2, CAMPAIGN_STATUS_CLOSED);
    verify(this.mockCampaignRepository).save(CAMPAIGN_2);
    verify(this.mockCampaignEventPublisher)
        .publishCampaignStoppedEvent(CAMPAIGN_2, List.of(ASSIGNEE_ID));
  }

  @Test
  void testActionComplete() {
    when(this.mockCampaignRepository.findById(CAMPAIGN_ID_2)).thenReturn(Optional.of(CAMPAIGN_2));
    when(this.mockCampaignRepository.save(CAMPAIGN_2)).thenReturn(CAMPAIGN_2);

    this.campaignService.action(CAMPAIGN_ID_2, CAMPAIGN_ACTION_COMPLETE, OWNER_ID);

    verify(this.mockStateMachine).transition(CAMPAIGN_2, CAMPAIGN_STATUS_COMPLETED);
    verify(this.mockCampaignRepository).save(CAMPAIGN_2);
  }

  @Test
  void testFindCampaignsById() {
    when(this.mockCampaignRepository.findByIdInAndIsDeletedFalse(CAMPAIGN_ID_SET))
        .thenReturn(CAMPAIGN_SET);

    final Set<Campaign> result = this.campaignService.findCampaignsById(CAMPAIGN_ID_SET);

    assertEquals(CAMPAIGN_SET, result);
  }

  @Test
  void testGetCampaignsForOwnerIncludesOwnedAndAssignedCampaigns() {
    final CampaignAssignment publishedAssignment =
        ASSIGNMENT_1.toBuilder()
            .campaignId(CAMPAIGN_ID_2)
            .assigneeId(REQUESTER_ID)
            .status(CAMPAIGN_ASSIGNMENT_STATUS_PUBLISHED)
            .build();
    when(this.mockCampaignRepository.findByOwnerIdAndIsDeletedFalse(REQUESTER_ID))
        .thenReturn(List.of(CAMPAIGN_1));
    when(this.mockCampaignAssignmentRepository.findByAssigneeIdAndStatus(
            REQUESTER_ID, CAMPAIGN_ASSIGNMENT_STATUS_PUBLISHED))
        .thenReturn(List.of(publishedAssignment));
    when(this.mockCampaignRepository.findByIdInAndIsDeletedFalse(Set.of(CAMPAIGN_ID_2)))
        .thenReturn(Set.of(CAMPAIGN_2));

    final List<Campaign> result = this.campaignService.getCampaignsForOwner(REQUESTER_ID);

    assertEquals(List.of(CAMPAIGN_1, CAMPAIGN_2), result);
  }

  @Test
  void testGetCampaignsForOwnerDoesNotDuplicateCampaignOwnedAndAssigned() {
    final CampaignAssignment publishedAssignment =
        ASSIGNMENT_1.toBuilder()
            .campaignId(CAMPAIGN_ID_1)
            .assigneeId(REQUESTER_ID)
            .status(CAMPAIGN_ASSIGNMENT_STATUS_PUBLISHED)
            .build();
    when(this.mockCampaignRepository.findByOwnerIdAndIsDeletedFalse(REQUESTER_ID))
        .thenReturn(List.of(CAMPAIGN_1));
    when(this.mockCampaignAssignmentRepository.findByAssigneeIdAndStatus(
            REQUESTER_ID, CAMPAIGN_ASSIGNMENT_STATUS_PUBLISHED))
        .thenReturn(List.of(publishedAssignment));
    when(this.mockCampaignRepository.findByIdInAndIsDeletedFalse(Set.of(CAMPAIGN_ID_1)))
        .thenReturn(Set.of(CAMPAIGN_1));

    final List<Campaign> result = this.campaignService.getCampaignsForOwner(REQUESTER_ID);

    assertEquals(List.of(CAMPAIGN_1), result);
  }

  @Test
  void testCopySuccess() {
    when(this.mockCampaignRepository.findById(CAMPAIGN_ID_1)).thenReturn(Optional.of(CAMPAIGN_1));
    when(this.mockCodeGenerationService.generateCodeFromSequence(WellKnownSequences.CAMPAIGN))
        .thenReturn(GENERATED_CODE);
    final ArgumentCaptor<Campaign> captor = ArgumentCaptor.forClass(Campaign.class);
    when(this.mockCampaignRepository.save(captor.capture())).thenReturn(EXPECTED_CAMPAIGN_1);

    final Campaign result = this.campaignService.copy(CAMPAIGN_ID_1, REQUESTER_ID);

    assertEquals(EXPECTED_CAMPAIGN_1, result);
    final Campaign savedCopy = captor.getValue();
    assertNull(savedCopy.getId());
    assertEquals(GENERATED_CODE, savedCopy.getCode());
    assertEquals(CAMPAIGN_STATUS_DRAFT, savedCopy.getStatus());
    assertEquals(REQUESTER_ID, savedCopy.getCreatedBy());
    assertEquals(REQUESTER_ID, savedCopy.getUpdatedBy());
    assertNull(savedCopy.getCreatedAt());
    assertNull(savedCopy.getUpdatedAt());
    assertNull(savedCopy.getAssignmentsDraft());
    verifyNoInteractions(this.mockCampaignAssignmentService);
  }

  @Test
  void testCopyWhenNoAssignments() {
    when(this.mockCampaignRepository.findById(CAMPAIGN_ID_1)).thenReturn(Optional.of(CAMPAIGN_1));
    when(this.mockCodeGenerationService.generateCodeFromSequence(WellKnownSequences.CAMPAIGN))
        .thenReturn(GENERATED_CODE);
    final ArgumentCaptor<Campaign> captor = ArgumentCaptor.forClass(Campaign.class);
    when(this.mockCampaignRepository.save(captor.capture())).thenReturn(EXPECTED_CAMPAIGN_1);

    this.campaignService.copy(CAMPAIGN_ID_1, REQUESTER_ID);

    verifyNoInteractions(this.mockCampaignAssignmentService);
  }

  @Test
  void testCopyWhenNotFound() {
    when(this.mockCampaignRepository.findById(CAMPAIGN_ID_1)).thenReturn(Optional.empty());

    assertThrows(
        NotFoundException.class, () -> this.campaignService.copy(CAMPAIGN_ID_1, REQUESTER_ID));
  }

  @Test
  void testSearch() {
    final Pageable pageable = PageRequest.of(0, 20);
    final Page<Campaign> campaignPage = new PageImpl<>(List.of(CAMPAIGN_1), pageable, 1);
    when(this.mockCampaignRepository.search(
            OWNER_ID,
            List.of("nike"),
            List.of(PLATFORM_AMAZON),
            List.of(CAMPAIGN_TYPE_REVIEW),
            List.of(CAMPAIGN_STATUS_ACTIVE),
            20240101,
            20241231,
            pageable))
        .thenReturn(campaignPage);
    when(this.mockCampaignSlotRepository.findByCampaignIdInAndIsDeletedFalse(
            List.of(CAMPAIGN_ID_1)))
        .thenReturn(List.of(SLOT_1));

    final Page<CampaignSummary> result =
        this.campaignService.search(
            OWNER_ID,
            new CampaignSearchCriteria(
                List.of("Nike"),
                List.of(PLATFORM_AMAZON),
                List.of(CAMPAIGN_TYPE_REVIEW),
                List.of(CAMPAIGN_STATUS_ACTIVE),
                20240101,
                20241231),
            pageable);

    assertEquals(1, result.getTotalElements());
    final CampaignSummary summary = result.getContent().get(0);
    assertEquals(CAMPAIGN_1, summary.getCampaign());
    assertEquals(SLOT_1.getTotalSlots() - SLOT_1.getSlotsAvailable(), summary.getSlotsClaimed());
  }

  @Test
  void testGetByOwnerIdPaged() {
    final Pageable pageable = PageRequest.of(0, 20);
    final Page<Campaign> campaignPage = new PageImpl<>(List.of(CAMPAIGN_1), pageable, 1);
    when(this.mockCampaignRepository.findByOwnerIdAndIsDeletedFalse(OWNER_ID, pageable))
        .thenReturn(campaignPage);
    when(this.mockCampaignSlotRepository.findByCampaignIdInAndIsDeletedFalse(
            List.of(CAMPAIGN_ID_1)))
        .thenReturn(List.of(SLOT_1));

    final Page<CampaignSummary> result = this.campaignService.getByOwnerId(OWNER_ID, pageable);

    assertEquals(1, result.getTotalElements());
    final CampaignSummary summary = result.getContent().get(0);
    assertEquals(CAMPAIGN_1, summary.getCampaign());
    assertEquals(SLOT_1.getTotalSlots() - SLOT_1.getSlotsAvailable(), summary.getSlotsClaimed());
  }

  @Test
  void testSearchNormalizesEmptyFilterListsToNull() {
    final Pageable pageable = PageRequest.of(0, 20);
    final Page<Campaign> campaignPage = new PageImpl<>(List.of(), pageable, 0);
    when(this.mockCampaignRepository.search(OWNER_ID, null, null, null, null, null, null, pageable))
        .thenReturn(campaignPage);
    when(this.mockCampaignSlotRepository.findByCampaignIdInAndIsDeletedFalse(List.of()))
        .thenReturn(List.of());

    final Page<CampaignSummary> result =
        this.campaignService.search(
            OWNER_ID,
            new CampaignSearchCriteria(List.of(), List.of(), List.of(), List.of(), null, null),
            pageable);

    assertEquals(0, result.getTotalElements());
    verify(this.mockCampaignRepository)
        .search(OWNER_ID, null, null, null, null, null, null, pageable);
  }
}
