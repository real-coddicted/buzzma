package com.coddicted.buzzma.campaign.processor;

import static com.coddicted.buzzma.campaign.processor.Fixtures.ASSIGNEE_ID;
import static com.coddicted.buzzma.campaign.processor.Fixtures.CAMPAIGN_1;
import static com.coddicted.buzzma.campaign.processor.Fixtures.CAMPAIGN_1_PUBLISHED;
import static com.coddicted.buzzma.campaign.processor.Fixtures.CAMPAIGN_ID_1;
import static com.coddicted.buzzma.campaign.processor.Fixtures.EXPECTED_ASSIGNMENT;
import static com.coddicted.buzzma.campaign.processor.Fixtures.EXPECTED_SLOT;
import static com.coddicted.buzzma.campaign.processor.Fixtures.PRODUCT_1;
import static com.coddicted.buzzma.campaign.processor.Fixtures.REQUESTER_ID;
import static com.coddicted.buzzma.campaign.processor.Fixtures.REQUEST_MIXED_SLOT_OFFERED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.coddicted.buzzma.campaign.dto.CampaignRequestDto;
import com.coddicted.buzzma.campaign.dto.CampaignResponseDto;
import com.coddicted.buzzma.campaign.dto.ShareCampaignResponseDto;
import com.coddicted.buzzma.campaign.entity.AdditionalRewardType;
import com.coddicted.buzzma.campaign.entity.Campaign;
import com.coddicted.buzzma.campaign.entity.CampaignAction;
import com.coddicted.buzzma.campaign.entity.CampaignAssignment;
import com.coddicted.buzzma.campaign.entity.CampaignShare;
import com.coddicted.buzzma.campaign.entity.CampaignSlot;
import com.coddicted.buzzma.campaign.entity.CampaignStatus;
import com.coddicted.buzzma.campaign.entity.CampaignStepType;
import com.coddicted.buzzma.campaign.entity.CampaignType;
import com.coddicted.buzzma.campaign.mapper.CampaignMapper;
import com.coddicted.buzzma.campaign.notification.CampaignEventPublisher;
import com.coddicted.buzzma.campaign.service.CampaignAssignmentService;
import com.coddicted.buzzma.campaign.service.CampaignService;
import com.coddicted.buzzma.campaign.service.CampaignShareService;
import com.coddicted.buzzma.campaign.service.CampaignSlotService;
import com.coddicted.buzzma.connection.service.ConnectionService;
import com.coddicted.buzzma.identity.service.UserService;
import com.coddicted.buzzma.shared.enums.Platform;
import com.coddicted.buzzma.shared.exception.BusinessRuleViolationException;
import java.math.BigInteger;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CampaignProcessorTest {

  @Mock private CampaignService campaignService;
  @Mock private CampaignMapper campaignMapper;
  @Mock private ProductProcessor productProcessor;
  @Mock private CampaignAssignmentService campaignAssignmentService;
  @Mock private CampaignSlotService campaignSlotService;
  @Mock private CampaignEventPublisher campaignEventPublisher;
  @Mock private ConnectionService connectionService;
  @Mock private UserService userService;
  @Mock private CampaignShareService campaignShareService;

  private CampaignProcessor campaignProcessor;

  @BeforeEach
  void setUp() {
    campaignProcessor =
        new CampaignProcessor(
            campaignService,
            campaignMapper,
            productProcessor,
            campaignAssignmentService,
            campaignSlotService,
            campaignEventPublisher,
            connectionService,
            userService,
            campaignShareService);
  }

  @Test
  void testCreateWithPastEndDateThrows() {
    final CampaignRequestDto requestWithPastEndDate =
        CampaignRequestDto.builder().endDate(20200101).build();

    final BusinessRuleViolationException ex =
        assertThrows(
            BusinessRuleViolationException.class,
            () -> campaignProcessor.create(REQUESTER_ID, requestWithPastEndDate));
    assertEquals("Campaign end date cannot be in the past", ex.getMessage());
  }

  @Test
  void testCreateAppReviewTypeOnNonAppStorePlatformThrows() {
    final CampaignRequestDto request =
        CampaignRequestDto.builder()
            .endDate(20991231)
            .platform(Platform.PLATFORM_AMAZON)
            .campaignType(CampaignType.CAMPAIGN_TYPE_APP_REVIEW)
            .build();

    final BusinessRuleViolationException ex =
        assertThrows(
            BusinessRuleViolationException.class,
            () -> campaignProcessor.create(REQUESTER_ID, request));
    assertEquals(
        "App-review campaigns are only allowed on Apple App Store or Google Play Store",
        ex.getMessage());
  }

  @Test
  void testCreateAppStorePlatformWithNonAppReviewTypeThrows() {
    final CampaignRequestDto request =
        CampaignRequestDto.builder()
            .endDate(20991231)
            .platform(Platform.PLATFORM_APPLE_APP_STORE)
            .campaignType(CampaignType.CAMPAIGN_TYPE_ORDER)
            .build();

    final BusinessRuleViolationException ex =
        assertThrows(
            BusinessRuleViolationException.class,
            () -> campaignProcessor.create(REQUESTER_ID, request));
    assertEquals(
        "Apple App Store and Google Play Store campaigns must be of type App Review",
        ex.getMessage());
  }

  @Test
  void testCreateCashbackRewardWithoutAmountThrows() {
    final CampaignRequestDto request =
        CampaignRequestDto.builder()
            .endDate(20991231)
            .additionalRewardType(AdditionalRewardType.CASHBACK)
            .build();

    final BusinessRuleViolationException ex =
        assertThrows(
            BusinessRuleViolationException.class,
            () -> campaignProcessor.create(REQUESTER_ID, request));
    assertEquals("A cashback reward requires a positive cashback amount", ex.getMessage());
  }

  @Test
  void testCreateCashbackAmountWithoutRewardTypeThrows() {
    final CampaignRequestDto request =
        CampaignRequestDto.builder()
            .endDate(20991231)
            .additionalRewardCashbackPaise(BigInteger.valueOf(500))
            .build();

    final BusinessRuleViolationException ex =
        assertThrows(
            BusinessRuleViolationException.class,
            () -> campaignProcessor.create(REQUESTER_ID, request));
    assertEquals(
        "Cashback amount is only allowed when the additional reward type is cashback",
        ex.getMessage());
  }

  // Campaign/CampaignSlot/CampaignAssignment are JPA entities without an equals() override, so
  // the instances CampaignProcessor builds internally (via toBuilder()/builder() calls) can never
  // equal a pre-built fixture object. ArgumentCaptor.capture() is used for those specific
  // arguments instead of any()/anyList() so the exact object the processor constructed can be
  // asserted on field-by-field below; every other stub/verify below matches on the precise
  // fixture value or object reference.
  @Test
  @SuppressWarnings("unchecked")
  void testCreateWithNegativeSlotOfferedExcludesAssigneeLikeZero() {
    final List<CampaignAssignment> expectedAssignments = List.of(EXPECTED_ASSIGNMENT);

    when(productProcessor.saveProduct(REQUEST_MIXED_SLOT_OFFERED)).thenReturn(PRODUCT_1);
    when(campaignMapper.toCampaignEntity(REQUEST_MIXED_SLOT_OFFERED)).thenReturn(CAMPAIGN_1);

    final ArgumentCaptor<Campaign> campaignCaptor = ArgumentCaptor.forClass(Campaign.class);
    when(campaignService.create(campaignCaptor.capture())).thenReturn(CAMPAIGN_1);

    final ArgumentCaptor<List<CampaignSlot>> slotsCaptor = ArgumentCaptor.forClass(List.class);
    when(campaignSlotService.create(slotsCaptor.capture())).thenReturn(List.of(EXPECTED_SLOT));

    final ArgumentCaptor<List<CampaignAssignment>> assignmentsCaptor =
        ArgumentCaptor.forClass(List.class);
    when(campaignAssignmentService.create(assignmentsCaptor.capture()))
        .thenReturn(expectedAssignments);

    when(campaignService.action(
            CAMPAIGN_ID_1, CampaignAction.CAMPAIGN_ACTION_PUBLISH, REQUESTER_ID))
        .thenReturn(CAMPAIGN_1_PUBLISHED);
    when(campaignMapper.toResponse(CAMPAIGN_1_PUBLISHED, expectedAssignments))
        .thenReturn(CampaignResponseDto.builder().build());

    campaignProcessor.create(REQUESTER_ID, REQUEST_MIXED_SLOT_OFFERED);

    assertFalse(campaignCaptor.getValue().isOpenToAll());

    assertEquals(1, slotsCaptor.getValue().size());
    assertEquals(5, slotsCaptor.getValue().get(0).getTotalSlots());

    assertEquals(1, assignmentsCaptor.getValue().size());
    assertEquals(5, assignmentsCaptor.getValue().get(0).getSlotLimit());
    assertEquals(ASSIGNEE_ID, assignmentsCaptor.getValue().get(0).getAssigneeId());

    verify(campaignMapper).toResponse(CAMPAIGN_1_PUBLISHED, expectedAssignments);
  }

  @Test
  void testUpdateCampaignOnNonDraftCampaignThrows() {
    final Campaign activeCampaign =
        CAMPAIGN_1.toBuilder().status(CampaignStatus.CAMPAIGN_STATUS_ACTIVE).build();
    when(campaignService.getById(CAMPAIGN_ID_1)).thenReturn(activeCampaign);

    final BusinessRuleViolationException ex =
        assertThrows(
            BusinessRuleViolationException.class,
            () ->
                campaignProcessor.updateCampaign(
                    REQUESTER_ID, CAMPAIGN_ID_1, CampaignRequestDto.builder().build()));
    assertEquals("Cannot update a campaign that is not in draft status", ex.getMessage());
  }

  @Test
  void testUpdateCampaignNormalizesRequiredStepsForcingOrderAndDroppingCashback() {
    final CampaignRequestDto request =
        CampaignRequestDto.builder()
            .requiredSteps(List.of(CampaignStepType.CASHBACK, CampaignStepType.REVIEW))
            .build();
    when(campaignService.getById(CAMPAIGN_ID_1)).thenReturn(CAMPAIGN_1);
    when(productProcessor.updateProduct(CAMPAIGN_1.getProduct(), request)).thenReturn(PRODUCT_1);

    final ArgumentCaptor<Campaign> captor = ArgumentCaptor.forClass(Campaign.class);
    when(campaignService.update(captor.capture())).thenReturn(CAMPAIGN_1);
    when(campaignMapper.toResponse(CAMPAIGN_1)).thenReturn(CampaignResponseDto.builder().build());

    campaignProcessor.updateCampaign(REQUESTER_ID, CAMPAIGN_ID_1, request);

    assertEquals(
        List.of(CampaignStepType.ORDER, CampaignStepType.REVIEW),
        captor.getValue().getRequiredSteps());
  }

  @Test
  void testShareCampaignWithBrandSuccess() {
    final UUID toUserId = ASSIGNEE_ID;
    final Campaign ownedCampaign = CAMPAIGN_1.toBuilder().ownerId(REQUESTER_ID).build();
    when(campaignService.getById(CAMPAIGN_ID_1)).thenReturn(ownedCampaign);
    when(connectionService.isParentOf(toUserId, REQUESTER_ID)).thenReturn(true);
    when(campaignShareService.existsByCampaignId(CAMPAIGN_ID_1)).thenReturn(false);

    final CampaignShare saved =
        CampaignShare.builder()
            .campaignId(CAMPAIGN_ID_1)
            .toUserId(toUserId)
            .fromUserId(REQUESTER_ID)
            .createdAt(Instant.EPOCH)
            .build();
    final ArgumentCaptor<CampaignShare> captor = ArgumentCaptor.forClass(CampaignShare.class);
    when(campaignShareService.create(captor.capture())).thenReturn(saved);

    final ShareCampaignResponseDto response =
        campaignProcessor.shareCampaign(REQUESTER_ID, CAMPAIGN_ID_1, toUserId);

    assertEquals(CAMPAIGN_ID_1, captor.getValue().getCampaignId());
    assertEquals(toUserId, captor.getValue().getToUserId());
    assertEquals(REQUESTER_ID, captor.getValue().getFromUserId());
    assertEquals(CAMPAIGN_ID_1, response.getCampaignId());
    assertEquals(toUserId, response.getToUserId());
  }

  @Test
  void testShareCampaignWithBrandWhenRequesterIsNotOwnerThrows() {
    when(campaignService.getById(CAMPAIGN_ID_1)).thenReturn(CAMPAIGN_1);

    final BusinessRuleViolationException ex =
        assertThrows(
            BusinessRuleViolationException.class,
            () -> campaignProcessor.shareCampaign(REQUESTER_ID, CAMPAIGN_ID_1, ASSIGNEE_ID));
    assertEquals("Only the campaign owner can share it with a brand", ex.getMessage());
  }

  @Test
  void testShareCampaignWithBrandWhenNotConnectedThrows() {
    final Campaign ownedCampaign = CAMPAIGN_1.toBuilder().ownerId(REQUESTER_ID).build();
    when(campaignService.getById(CAMPAIGN_ID_1)).thenReturn(ownedCampaign);
    when(connectionService.isParentOf(ASSIGNEE_ID, REQUESTER_ID)).thenReturn(false);

    final BusinessRuleViolationException ex =
        assertThrows(
            BusinessRuleViolationException.class,
            () -> campaignProcessor.shareCampaign(REQUESTER_ID, CAMPAIGN_ID_1, ASSIGNEE_ID));
    assertEquals("Brand is not connected to this agency", ex.getMessage());
  }

  @Test
  void testShareCampaignWithBrandSucceedsWhenBrandInvitedAgency() {
    final Campaign ownedCampaign = CAMPAIGN_1.toBuilder().ownerId(REQUESTER_ID).build();
    when(campaignService.getById(CAMPAIGN_ID_1)).thenReturn(ownedCampaign);
    when(connectionService.isParentOf(ASSIGNEE_ID, REQUESTER_ID)).thenReturn(true);
    when(campaignShareService.existsByCampaignId(CAMPAIGN_ID_1)).thenReturn(false);
    final ArgumentCaptor<CampaignShare> captor = ArgumentCaptor.forClass(CampaignShare.class);
    when(campaignShareService.create(captor.capture()))
        .thenReturn(
            CampaignShare.builder()
                .campaignId(CAMPAIGN_ID_1)
                .toUserId(ASSIGNEE_ID)
                .fromUserId(REQUESTER_ID)
                .createdAt(Instant.EPOCH)
                .build());

    final ShareCampaignResponseDto response =
        campaignProcessor.shareCampaign(REQUESTER_ID, CAMPAIGN_ID_1, ASSIGNEE_ID);

    assertEquals(CAMPAIGN_ID_1, response.getCampaignId());
    assertEquals(ASSIGNEE_ID, response.getToUserId());
    assertEquals(REQUESTER_ID, captor.getValue().getFromUserId());
  }

  @Test
  void testShareCampaignWithBrandWhenAlreadySharedThrows() {
    final Campaign ownedCampaign = CAMPAIGN_1.toBuilder().ownerId(REQUESTER_ID).build();
    when(campaignService.getById(CAMPAIGN_ID_1)).thenReturn(ownedCampaign);
    when(connectionService.isParentOf(ASSIGNEE_ID, REQUESTER_ID)).thenReturn(true);
    when(campaignShareService.existsByCampaignId(CAMPAIGN_ID_1)).thenReturn(true);

    final BusinessRuleViolationException ex =
        assertThrows(
            BusinessRuleViolationException.class,
            () -> campaignProcessor.shareCampaign(REQUESTER_ID, CAMPAIGN_ID_1, ASSIGNEE_ID));
    assertEquals("Campaign is already shared with a brand", ex.getMessage());
  }
}
