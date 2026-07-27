package com.coddicted.buzzma.campaign.service;

import static com.coddicted.buzzma.campaign.entity.CampaignStatus.CAMPAIGN_STATUS_ACTIVE;
import static com.coddicted.buzzma.campaign.entity.CampaignStatus.CAMPAIGN_STATUS_ASSIGNED;
import static com.coddicted.buzzma.campaign.entity.CampaignStatus.CAMPAIGN_STATUS_CLOSED;
import static com.coddicted.buzzma.campaign.entity.CampaignStatus.CAMPAIGN_STATUS_COMPLETED;
import static com.coddicted.buzzma.campaign.entity.CampaignStatus.CAMPAIGN_STATUS_DRAFT;
import static com.coddicted.buzzma.campaign.entity.CampaignStatus.CAMPAIGN_STATUS_PAUSED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.coddicted.buzzma.campaign.entity.Campaign;
import com.coddicted.buzzma.shared.exception.InvalidStateTransitionException;
import org.junit.jupiter.api.Test;

class CampaignStateMachineTest {

  private final CampaignStateMachine stateMachine = new CampaignStateMachine();

  @Test
  void testDraftCannotTransitionToClosed() {
    final Campaign campaign = Campaign.builder().status(CAMPAIGN_STATUS_DRAFT).build();

    assertThrows(
        InvalidStateTransitionException.class,
        () -> this.stateMachine.transition(campaign, CAMPAIGN_STATUS_CLOSED));
  }

  @Test
  void testAssignedCanTransitionToClosed() {
    final Campaign campaign = Campaign.builder().status(CAMPAIGN_STATUS_ASSIGNED).build();

    this.stateMachine.transition(campaign, CAMPAIGN_STATUS_CLOSED);

    assertEquals(CAMPAIGN_STATUS_CLOSED, campaign.getStatus());
  }

  @Test
  void testAssignedCannotTransitionToActive() {
    final Campaign campaign = Campaign.builder().status(CAMPAIGN_STATUS_ASSIGNED).build();

    assertThrows(
        InvalidStateTransitionException.class,
        () -> this.stateMachine.transition(campaign, CAMPAIGN_STATUS_ACTIVE));
  }

  @Test
  void testCompletedCannotTransitionToClosed() {
    final Campaign campaign = Campaign.builder().status(CAMPAIGN_STATUS_COMPLETED).build();

    assertThrows(
        InvalidStateTransitionException.class,
        () -> this.stateMachine.transition(campaign, CAMPAIGN_STATUS_CLOSED));
  }

  @Test
  void testActiveCanStillTransitionToPaused() {
    final Campaign campaign = Campaign.builder().status(CAMPAIGN_STATUS_ACTIVE).build();

    this.stateMachine.transition(campaign, CAMPAIGN_STATUS_PAUSED);

    assertEquals(CAMPAIGN_STATUS_PAUSED, campaign.getStatus());
  }
}
