package com.coddicted.buzzma.campaign.service.impl;

import static com.coddicted.buzzma.campaign.entity.CampaignAction.CAMPAIGN_ACTION_CLOSE;
import static com.coddicted.buzzma.campaign.entity.CampaignAction.CAMPAIGN_ACTION_COMPLETE;
import static com.coddicted.buzzma.campaign.entity.CampaignAction.CAMPAIGN_ACTION_PAUSE;
import static com.coddicted.buzzma.campaign.entity.CampaignAction.CAMPAIGN_ACTION_PUBLISH;
import static com.coddicted.buzzma.campaign.entity.CampaignAction.CAMPAIGN_ACTION_RESUME;
import static com.coddicted.buzzma.campaign.entity.CampaignAssignmentStatus.CAMPAIGN_ASSIGNMENT_STATUS_LOCKED;
import static com.coddicted.buzzma.campaign.entity.CampaignStatus.CAMPAIGN_STATUS_ACTIVE;
import static com.coddicted.buzzma.campaign.entity.CampaignStatus.CAMPAIGN_STATUS_CLOSED;
import static com.coddicted.buzzma.campaign.entity.CampaignStatus.CAMPAIGN_STATUS_COMPLETED;
import static com.coddicted.buzzma.campaign.entity.CampaignStatus.CAMPAIGN_STATUS_DRAFT;
import static com.coddicted.buzzma.campaign.entity.CampaignStatus.CAMPAIGN_STATUS_PAUSED;
import static com.coddicted.buzzma.campaign.service.impl.Fixtures.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.coddicted.buzzma.campaign.entity.Campaign;
import com.coddicted.buzzma.campaign.notification.CampaignEventPublisher;
import com.coddicted.buzzma.campaign.persistence.CampaignAssignmentRepository;
import com.coddicted.buzzma.campaign.persistence.CampaignRepository;
import com.coddicted.buzzma.campaign.persistence.CampaignSlotRepository;
import com.coddicted.buzzma.campaign.service.CampaignAssignmentService;
import com.coddicted.buzzma.campaign.service.CampaignStateMachine;
import com.coddicted.buzzma.shared.exception.BusinessRuleViolationException;
import com.coddicted.buzzma.shared.exception.ForbiddenException;
import com.coddicted.buzzma.shared.exception.NotFoundException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CampaignServiceImplTest {

  @Mock private CampaignRepository mockCampaignRepository;
  @Mock private CampaignAssignmentRepository mockCampaignAssignmentRepository;
  @Mock private CampaignAssignmentService mockCampaignAssignmentService;
  @Mock private CampaignSlotRepository mockCampaignSlotRepository;
  @Mock private CampaignStateMachine mockStateMachine;
  @Mock private CampaignEventPublisher mockCampaignEventPublisher;

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
            this.mockCampaignEventPublisher);
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
    when(this.mockCampaignRepository.save(CAMPAIGN_1)).thenReturn(CAMPAIGN_1);

    final Campaign result = this.campaignService.create(CAMPAIGN_1);

    assertEquals(CAMPAIGN_1, result);
  }

  @Test
  void testUpdate() {
    when(this.mockCampaignRepository.save(CAMPAIGN_1)).thenReturn(CAMPAIGN_1);

    final Campaign result = this.campaignService.update(CAMPAIGN_1);

    assertEquals(CAMPAIGN_1, result);
  }

  @Test
  void testDelete() {
    when(this.mockCampaignRepository.findById(CAMPAIGN_ID_1)).thenReturn(Optional.of(CAMPAIGN_1));

    this.campaignService.delete(CAMPAIGN_ID_1, REQUESTER_ID);

    final ArgumentCaptor<Campaign> captor = ArgumentCaptor.forClass(Campaign.class);
    verify(this.mockCampaignRepository).save(captor.capture());
    final Campaign saved = captor.getValue();
    assertEquals(CAMPAIGN_ID_1, saved.getId());
    assertTrue(saved.isDeleted());
    assertEquals(REQUESTER_ID, saved.getUpdatedBy());
  }

  @Test
  void testDeleteWhenNotFound() {
    when(this.mockCampaignRepository.findById(CAMPAIGN_ID_1)).thenReturn(Optional.empty());

    assertThrows(
        NotFoundException.class, () -> this.campaignService.delete(CAMPAIGN_ID_1, REQUESTER_ID));
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
  }

  @Test
  void testActionPublishWhenNotOwner() {
    when(this.mockCampaignRepository.findById(CAMPAIGN_ID_1)).thenReturn(Optional.of(CAMPAIGN_1));

    final ForbiddenException ex =
        assertThrows(
            ForbiddenException.class,
            () ->
                this.campaignService.action(CAMPAIGN_ID_1, CAMPAIGN_ACTION_PUBLISH, NON_OWNER_ID));
    assertEquals("Only the campaign owner can publish this campaign", ex.getMessage());
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

    this.campaignService.action(CAMPAIGN_ID_2, CAMPAIGN_ACTION_PAUSE, OWNER_ID);

    verify(this.mockStateMachine).transition(CAMPAIGN_2, CAMPAIGN_STATUS_PAUSED);
    verify(this.mockCampaignRepository).save(CAMPAIGN_2);
  }

  @Test
  void testActionResume() {
    when(this.mockCampaignRepository.findById(CAMPAIGN_ID_3)).thenReturn(Optional.of(CAMPAIGN_3));
    when(this.mockCampaignRepository.save(CAMPAIGN_3)).thenReturn(CAMPAIGN_3);

    this.campaignService.action(CAMPAIGN_ID_3, CAMPAIGN_ACTION_RESUME, OWNER_ID);

    verify(this.mockStateMachine).transition(CAMPAIGN_3, CAMPAIGN_STATUS_ACTIVE);
    verify(this.mockCampaignRepository).save(CAMPAIGN_3);
  }

  @Test
  void testActionClose() {
    when(this.mockCampaignRepository.findById(CAMPAIGN_ID_2)).thenReturn(Optional.of(CAMPAIGN_2));
    when(this.mockCampaignRepository.save(CAMPAIGN_2)).thenReturn(CAMPAIGN_2);

    this.campaignService.action(CAMPAIGN_ID_2, CAMPAIGN_ACTION_CLOSE, OWNER_ID);

    verify(this.mockStateMachine).transition(CAMPAIGN_2, CAMPAIGN_STATUS_CLOSED);
    verify(this.mockCampaignRepository).save(CAMPAIGN_2);
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
  void testCopySuccess() {
    when(this.mockCampaignRepository.findById(CAMPAIGN_ID_1)).thenReturn(Optional.of(CAMPAIGN_1));
    when(this.mockCampaignAssignmentRepository.findByCampaignId(CAMPAIGN_ID_1))
        .thenReturn(List.of(ASSIGNMENT_1));
    final ArgumentCaptor<Campaign> campaignCaptor = ArgumentCaptor.forClass(Campaign.class);
    when(this.mockCampaignRepository.save(campaignCaptor.capture()))
        .thenReturn(EXPECTED_CAMPAIGN_1);

    final Campaign result = this.campaignService.copy(CAMPAIGN_ID_1, REQUESTER_ID);

    assertEquals(EXPECTED_CAMPAIGN_1, result);
    final Campaign savedCopy = campaignCaptor.getValue();
    assertNull(savedCopy.getId());
    assertEquals(CAMPAIGN_STATUS_DRAFT, savedCopy.getStatus());
    assertEquals(REQUESTER_ID, savedCopy.getCreatedBy());
    assertEquals(REQUESTER_ID, savedCopy.getUpdatedBy());
    assertNull(savedCopy.getCreatedAt());
    assertNull(savedCopy.getUpdatedAt());
    verify(this.mockCampaignAssignmentService)
        .copy(List.of(ASSIGNMENT_ID_1), SAVED_CAMPAIGN_ID, REQUESTER_ID);
  }

  @Test
  void testCopyWhenNoAssignments() {
    when(this.mockCampaignRepository.findById(CAMPAIGN_ID_1)).thenReturn(Optional.of(CAMPAIGN_1));
    when(this.mockCampaignAssignmentRepository.findByCampaignId(CAMPAIGN_ID_1))
        .thenReturn(List.of());
    final ArgumentCaptor<Campaign> campaignCaptor = ArgumentCaptor.forClass(Campaign.class);
    when(this.mockCampaignRepository.save(campaignCaptor.capture()))
        .thenReturn(EXPECTED_CAMPAIGN_1);

    this.campaignService.copy(CAMPAIGN_ID_1, REQUESTER_ID);

    verifyNoInteractions(this.mockCampaignAssignmentService);
  }

  @Test
  void testCopyWhenNotFound() {
    when(this.mockCampaignRepository.findById(CAMPAIGN_ID_1)).thenReturn(Optional.empty());

    assertThrows(
        NotFoundException.class, () -> this.campaignService.copy(CAMPAIGN_ID_1, REQUESTER_ID));
  }
}
