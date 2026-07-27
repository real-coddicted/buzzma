package com.coddicted.buzzma.campaign.scheduler;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.coddicted.buzzma.campaign.entity.Campaign;
import com.coddicted.buzzma.campaign.entity.CampaignAction;
import com.coddicted.buzzma.campaign.entity.CampaignStatus;
import com.coddicted.buzzma.campaign.service.CampaignService;
import com.coddicted.buzzma.shared.constants.WellKnownSystemActors;
import com.coddicted.buzzma.shared.exception.InvalidStateTransitionException;
import com.coddicted.buzzma.shared.util.DateTimeUtils;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CampaignAutoCloseSchedulerTest {

  private static final UUID CAMPAIGN_ID_1 = UUID.fromString("11111111-1111-1111-1111-111111111111");
  private static final UUID CAMPAIGN_ID_2 = UUID.fromString("22222222-2222-2222-2222-222222222222");
  private static final UUID OWNER_ID = UUID.fromString("44444444-4444-4444-4444-444444444444");
  private static final int TODAY =
      DateTimeUtils.toIntDate(LocalDate.now(ZoneId.of("Asia/Kolkata")));
  private static final List<CampaignStatus> ELIGIBLE_STATUSES =
      List.of(
          CampaignStatus.CAMPAIGN_STATUS_ACTIVE,
          CampaignStatus.CAMPAIGN_STATUS_ASSIGNED,
          CampaignStatus.CAMPAIGN_STATUS_PAUSED);

  @Mock private CampaignService mockCampaignService;

  private CampaignAutoCloseScheduler scheduler;

  @BeforeEach
  void setUp() {
    this.scheduler = new CampaignAutoCloseScheduler(this.mockCampaignService, 50);
  }

  @Test
  void testCloseExpiredCampaignsClosesEachExpiredCampaignAsSystem() {
    final Campaign campaign1 =
        Campaign.builder()
            .id(CAMPAIGN_ID_1)
            .ownerId(OWNER_ID)
            .status(CampaignStatus.CAMPAIGN_STATUS_ACTIVE)
            .build();
    final Campaign campaign2 =
        Campaign.builder()
            .id(CAMPAIGN_ID_2)
            .ownerId(OWNER_ID)
            .status(CampaignStatus.CAMPAIGN_STATUS_PAUSED)
            .build();
    when(this.mockCampaignService.findExpiredCampaigns(ELIGIBLE_STATUSES, TODAY, 50))
        .thenReturn(List.of(campaign1, campaign2));

    this.scheduler.closeExpiredCampaigns();

    verify(this.mockCampaignService)
        .action(
            CAMPAIGN_ID_1,
            CampaignAction.CAMPAIGN_ACTION_CLOSE,
            WellKnownSystemActors.SYSTEM_USER_ID);
    verify(this.mockCampaignService)
        .action(
            CAMPAIGN_ID_2,
            CampaignAction.CAMPAIGN_ACTION_CLOSE,
            WellKnownSystemActors.SYSTEM_USER_ID);
  }

  @Test
  void testCloseExpiredCampaignsDoesNothingWhenNoneExpired() {
    when(this.mockCampaignService.findExpiredCampaigns(ELIGIBLE_STATUSES, TODAY, 50))
        .thenReturn(List.of());

    this.scheduler.closeExpiredCampaigns();

    verify(this.mockCampaignService, never())
        .action(
            CAMPAIGN_ID_1,
            CampaignAction.CAMPAIGN_ACTION_CLOSE,
            WellKnownSystemActors.SYSTEM_USER_ID);
  }

  @Test
  void testCloseExpiredCampaignsContinuesAfterOneCampaignFailsToClose() {
    final Campaign campaign1 =
        Campaign.builder()
            .id(CAMPAIGN_ID_1)
            .ownerId(OWNER_ID)
            .status(CampaignStatus.CAMPAIGN_STATUS_ACTIVE)
            .build();
    final Campaign campaign2 =
        Campaign.builder()
            .id(CAMPAIGN_ID_2)
            .ownerId(OWNER_ID)
            .status(CampaignStatus.CAMPAIGN_STATUS_PAUSED)
            .build();
    when(this.mockCampaignService.findExpiredCampaigns(ELIGIBLE_STATUSES, TODAY, 50))
        .thenReturn(List.of(campaign1, campaign2));
    when(this.mockCampaignService.action(
            CAMPAIGN_ID_1,
            CampaignAction.CAMPAIGN_ACTION_CLOSE,
            WellKnownSystemActors.SYSTEM_USER_ID))
        .thenThrow(new InvalidStateTransitionException("boom"));

    this.scheduler.closeExpiredCampaigns();

    verify(this.mockCampaignService)
        .action(
            CAMPAIGN_ID_2,
            CampaignAction.CAMPAIGN_ACTION_CLOSE,
            WellKnownSystemActors.SYSTEM_USER_ID);
  }
}
