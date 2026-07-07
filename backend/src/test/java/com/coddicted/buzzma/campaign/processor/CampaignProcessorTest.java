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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.coddicted.buzzma.campaign.dto.CampaignResponseDto;
import com.coddicted.buzzma.campaign.entity.Campaign;
import com.coddicted.buzzma.campaign.entity.CampaignAction;
import com.coddicted.buzzma.campaign.entity.CampaignAssignment;
import com.coddicted.buzzma.campaign.entity.CampaignSlot;
import com.coddicted.buzzma.campaign.mapper.CampaignMapper;
import com.coddicted.buzzma.campaign.notification.CampaignEventPublisher;
import com.coddicted.buzzma.campaign.service.CampaignAssignmentService;
import com.coddicted.buzzma.campaign.service.CampaignService;
import com.coddicted.buzzma.campaign.service.CampaignSlotService;
import com.coddicted.buzzma.connection.service.ConnectionService;
import com.coddicted.buzzma.identity.service.UserService;
import java.util.List;
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
            userService);
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
}
