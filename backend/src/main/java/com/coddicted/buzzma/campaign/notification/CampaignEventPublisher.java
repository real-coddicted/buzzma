package com.coddicted.buzzma.campaign.notification;

import com.coddicted.buzzma.campaign.entity.Campaign;
import com.coddicted.buzzma.identity.service.UserService;
import com.coddicted.buzzma.notification.publisher.EventPublisher;
import com.coddicted.buzzma.notification.service.NotificationService;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class CampaignEventPublisher {

  private static final String PAGE_CAMPAIGNS = "campaigns";
  private static final String PAGE_ASSIGNMENTS = "assignments";

  private final NotificationService notificationService;
  private final EventPublisher eventPublisher;
  private final UserService userService;

  public CampaignEventPublisher(
      final NotificationService notificationService,
      final EventPublisher eventPublisher,
      final UserService userService) {
    this.notificationService = notificationService;
    this.eventPublisher = eventPublisher;
    this.userService = userService;
  }

  @Async
  public void publishCampaignCreatedEvent(final UUID campaignId, final UUID ownerId) {
    log.info("Publishing campaign created event for campaign id: {}", campaignId);
    this.notificationService.create(
        "New Campaign", "A new campaign has been created.", ownerId, ownerId);
    this.eventPublisher.publishRefresh(ownerId, PAGE_CAMPAIGNS);
  }

  @Async
  public void publishCampaignLaunchedEvent(final Campaign campaign, final List<UUID> assigneeIds) {
    publishCampaignStatusEvent(campaign, assigneeIds, "Campaign Launched");
  }

  @Async
  public void publishCampaignPausedEvent(final Campaign campaign, final List<UUID> assigneeIds) {
    publishCampaignStatusEvent(campaign, assigneeIds, "Campaign Paused");
  }

  @Async
  public void publishCampaignStoppedEvent(final Campaign campaign, final List<UUID> assigneeIds) {
    publishCampaignStatusEvent(campaign, assigneeIds, "Campaign Stopped");
  }

  @Async
  public void publishCampaignResumedEvent(final Campaign campaign, final List<UUID> assigneeIds) {
    publishCampaignStatusEvent(campaign, assigneeIds, "Campaign Resumed");
  }

  private void publishCampaignStatusEvent(
      final Campaign campaign, final List<UUID> assigneeIds, final String title) {
    log.info("Publishing '{}' event for campaign id: {}", title, campaign.getId());
    final String ownerName = this.userService.getById(campaign.getOwnerId()).getName();
    final String message = campaign.getTitle() + " (" + campaign.getCode() + ") by " + ownerName;
    this.notificationService.create(title, message, assigneeIds, campaign.getOwnerId());
    assigneeIds.forEach(
        assigneeId -> this.eventPublisher.publishRefresh(assigneeId, PAGE_ASSIGNMENTS));
  }
}
