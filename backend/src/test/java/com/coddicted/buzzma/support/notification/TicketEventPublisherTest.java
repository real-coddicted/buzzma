package com.coddicted.buzzma.support.notification;

import static org.mockito.Mockito.verify;

import com.coddicted.buzzma.notification.publisher.EventPublisher;
import com.coddicted.buzzma.notification.service.NotificationService;
import com.coddicted.buzzma.support.entity.Ticket;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TicketEventPublisherTest {

  private static final UUID RAISED_BY_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
  private static final UUID ASSIGNEE_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");

  @Mock private NotificationService mockNotificationService;
  @Mock private EventPublisher mockEventPublisher;

  @Test
  void testPublishTicketCreatedEventIncludesTitleCodeAndClaimCode() {
    final TicketEventPublisher publisher =
        new TicketEventPublisher(this.mockNotificationService, this.mockEventPublisher);
    final Ticket ticket =
        Ticket.builder()
            .id(UUID.randomUUID())
            .code("H9GT-W8E6")
            .title("Order not delivered")
            .raisedBy(RAISED_BY_ID)
            .assigneeId(ASSIGNEE_ID)
            .claimCode("KF7P-3GWH")
            .build();

    publisher.publishTicketCreatedEvent(ticket);

    verify(this.mockNotificationService)
        .create(
            "Ticket Assigned",
            "A new ticket has been assigned: Order not delivered (H9GT-W8E6) · Claim Code: KF7P-3GWH",
            ASSIGNEE_ID,
            RAISED_BY_ID);
  }

  @Test
  void testPublishTicketCreatedEventIncludesCampaignCodeWhenNoClaimCode() {
    final TicketEventPublisher publisher =
        new TicketEventPublisher(this.mockNotificationService, this.mockEventPublisher);
    final Ticket ticket =
        Ticket.builder()
            .id(UUID.randomUUID())
            .code("CMP1-A2B3")
            .title("Campaign question")
            .raisedBy(RAISED_BY_ID)
            .assigneeId(ASSIGNEE_ID)
            .campaignCode("CMP-001")
            .build();

    publisher.publishTicketCreatedEvent(ticket);

    verify(this.mockNotificationService)
        .create(
            "Ticket Assigned",
            "A new ticket has been assigned: Campaign question (CMP1-A2B3) · Campaign Code: CMP-001",
            ASSIGNEE_ID,
            RAISED_BY_ID);
  }
}
