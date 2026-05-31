package com.coddicted.buzzma.campaign.notification;

import com.coddicted.buzzma.notification.publisher.EventPublisher;
import com.coddicted.buzzma.notification.service.NotificationService;
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

  public CampaignEventPublisher(
      final NotificationService notificationService, final EventPublisher eventPublisher) {
    this.notificationService = notificationService;
    this.eventPublisher = eventPublisher;
  }

  @Async
  public void publishCampaignCreatedEvent(final UUID campaignId, final UUID ownerId) {
    log.info("Publishing campaign created event for campaign id: {}", campaignId);
    this.notificationService.create(
        "New Campaign", "A new campaign has been created.", ownerId, ownerId);
    this.eventPublisher.publishRefresh(ownerId, PAGE_CAMPAIGNS);
  }
}
