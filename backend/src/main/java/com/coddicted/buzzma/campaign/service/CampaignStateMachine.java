package com.coddicted.buzzma.campaign.service;

import static com.coddicted.buzzma.campaign.entity.CampaignStatus.CAMPAIGN_STATUS_ACTIVE;
import static com.coddicted.buzzma.campaign.entity.CampaignStatus.CAMPAIGN_STATUS_CLOSED;
import static com.coddicted.buzzma.campaign.entity.CampaignStatus.CAMPAIGN_STATUS_COMPLETED;
import static com.coddicted.buzzma.campaign.entity.CampaignStatus.CAMPAIGN_STATUS_DRAFT;
import static com.coddicted.buzzma.campaign.entity.CampaignStatus.CAMPAIGN_STATUS_PAUSED;

import com.coddicted.buzzma.campaign.entity.Campaign;
import com.coddicted.buzzma.campaign.entity.CampaignStatus;
import com.coddicted.buzzma.shared.exception.InvalidStateTransitionException;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class CampaignStateMachine {

  private static final Map<CampaignStatus, Set<CampaignStatus>> TRANSITIONS =
      Map.of(
          CAMPAIGN_STATUS_DRAFT, Set.of(CAMPAIGN_STATUS_ACTIVE),
          CAMPAIGN_STATUS_ACTIVE,
              Set.of(CAMPAIGN_STATUS_PAUSED, CAMPAIGN_STATUS_CLOSED, CAMPAIGN_STATUS_COMPLETED),
          CAMPAIGN_STATUS_PAUSED, Set.of(CAMPAIGN_STATUS_ACTIVE, CAMPAIGN_STATUS_CLOSED));

  public void transition(final Campaign campaign, final CampaignStatus to) {
    final CampaignStatus from = campaign.getStatus();
    if (!TRANSITIONS.getOrDefault(from, Set.of()).contains(to)) {
      throw new InvalidStateTransitionException(
          "Cannot transition campaign from " + from + " to " + to);
    }
    campaign.setStatus(to);
  }
}
