package com.coddicted.buzzma.campaign.scheduler;

import com.coddicted.buzzma.campaign.entity.Campaign;
import com.coddicted.buzzma.campaign.entity.CampaignAction;
import com.coddicted.buzzma.campaign.entity.CampaignStatus;
import com.coddicted.buzzma.campaign.service.CampaignService;
import com.coddicted.buzzma.shared.constants.WellKnownSystemActors;
import com.coddicted.buzzma.shared.util.DateTimeUtils;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class CampaignAutoCloseScheduler {

  private static final Logger LOGGER = LoggerFactory.getLogger(CampaignAutoCloseScheduler.class);
  private static final ZoneId ZONE = ZoneId.of("Asia/Kolkata");
  private static final List<CampaignStatus> ELIGIBLE_STATUSES =
      List.of(
          CampaignStatus.CAMPAIGN_STATUS_ACTIVE,
          CampaignStatus.CAMPAIGN_STATUS_ASSIGNED,
          CampaignStatus.CAMPAIGN_STATUS_PAUSED);

  private final CampaignService campaignService;
  private final int batchSize;

  public CampaignAutoCloseScheduler(
      final CampaignService campaignService,
      @Value("${app.campaign.auto-close.batch-size:50}") final int batchSize) {
    this.campaignService = campaignService;
    this.batchSize = batchSize;
  }

  @Scheduled(fixedDelayString = "${app.campaign.auto-close.fixed-delay-ms:900000}")
  public void closeExpiredCampaigns() {
    final int today = DateTimeUtils.toIntDate(LocalDate.now(ZONE));
    final List<Campaign> expired =
        campaignService.findExpiredCampaigns(ELIGIBLE_STATUSES, today, batchSize);
    if (expired.isEmpty()) {
      return;
    }
    LOGGER.info("CampaignAutoCloseScheduler: closing {} expired campaign(s)", expired.size());
    expired.forEach(
        campaign -> {
          try {
            campaignService.action(
                campaign.getId(),
                CampaignAction.CAMPAIGN_ACTION_CLOSE,
                WellKnownSystemActors.SYSTEM_USER_ID);
          } catch (final Exception e) {
            LOGGER.error(
                "CampaignAutoCloseScheduler: failed to close campaign {}: {}",
                campaign.getId(),
                e.getMessage(),
                e);
          }
        });
  }
}
