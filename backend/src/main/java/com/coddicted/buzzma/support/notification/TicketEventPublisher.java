package com.coddicted.buzzma.support.notification;

import com.coddicted.buzzma.notification.publisher.EventPublisher;
import com.coddicted.buzzma.notification.service.NotificationService;
import com.coddicted.buzzma.support.entity.Ticket;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Slf4j
@Service
public class TicketEventPublisher {
  private static final String PAGE_RAISED = "ticket_raised";
  private static final String PAGE_ASSIGNED = "ticket_assigned";

  private final NotificationService notificationService;
  private final EventPublisher eventPublisher;

  public TicketEventPublisher(
      final NotificationService notificationService, final EventPublisher eventPublisher) {
    this.notificationService = notificationService;
    this.eventPublisher = eventPublisher;
  }

  @Async
  public void publishTicketCreatedEvent(final Ticket ticket) {
    log.info("Publishing ticket created event for ticket id: {}", ticket.getId());
    final UUID raisedById = ticket.getRaisedBy();
    final UUID assigneeId = ticket.getAssigneeId();
    this.notificationService.create(
        "Ticket Assigned", buildAssignedMessage(ticket), assigneeId, raisedById);
    this.eventPublisher.publishRefresh(raisedById, PAGE_RAISED);
    this.eventPublisher.publishRefresh(assigneeId, PAGE_ASSIGNED);
  }

  private String buildAssignedMessage(final Ticket ticket) {
    final StringBuilder message =
        new StringBuilder("A new ticket has been assigned: ")
            .append(ticket.getTitle())
            .append(" (")
            .append(ticket.getCode())
            .append(")");
    if (StringUtils.hasText(ticket.getClaimCode())) {
      message.append(" · Claim Code: ").append(ticket.getClaimCode());
    } else if (StringUtils.hasText(ticket.getCampaignCode())) {
      message.append(" · Campaign Code: ").append(ticket.getCampaignCode());
    }
    return message.toString();
  }
}
