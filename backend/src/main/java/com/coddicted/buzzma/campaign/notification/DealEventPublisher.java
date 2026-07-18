package com.coddicted.buzzma.campaign.notification;

import com.coddicted.buzzma.campaign.entity.Campaign;
import com.coddicted.buzzma.campaign.entity.Deal;
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
public class DealEventPublisher {

  private final NotificationService notificationService;
  private final EventPublisher eventPublisher;
  private final UserService userService;

  public DealEventPublisher(
      final NotificationService notificationService,
      final EventPublisher eventPublisher,
      final UserService userService) {
    this.notificationService = notificationService;
    this.eventPublisher = eventPublisher;
    this.userService = userService;
  }

  @Async
  public void publishDealPublishedEvent(final Deal deal, final List<UUID> buyerIds) {
    if (buyerIds.isEmpty()) {
      return;
    }
    log.info("Publishing deal published event for deal id: {}", deal.getId());
    final String mediatorName = this.userService.getById(deal.getOwnerId()).getName();
    final Campaign campaign = deal.getCampaign();
    final String message = campaign.getTitle() + " (" + deal.getCode() + ") by " + mediatorName;
    this.notificationService.create("New Deal Published", message, buyerIds, deal.getOwnerId());
  }
}
