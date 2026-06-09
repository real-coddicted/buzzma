package com.coddicted.buzzma.support.strategy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import com.coddicted.buzzma.connection.entity.Connection;
import com.coddicted.buzzma.connection.entity.ConnectionStatus;
import com.coddicted.buzzma.connection.service.ConnectionService;
import com.coddicted.buzzma.identity.entity.BuzzmaUser;
import com.coddicted.buzzma.identity.entity.UserRole;
import com.coddicted.buzzma.identity.service.UserService;
import com.coddicted.buzzma.support.entity.Ticket;
import com.coddicted.buzzma.support.entity.TicketCategory;
import com.coddicted.buzzma.support.router.SimpleTicketRouter;
import com.coddicted.buzzma.support.service.TicketCategoryService;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TicketRouterTest {

  private static final UUID TECHNICAL_CATEGORY_ID =
      UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
  private static final UUID CLAIM_CATEGORY_ID =
      UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
  private static final UUID RAISER_ID = UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc");
  private static final UUID ADMIN_ID = UUID.fromString("dddddddd-dddd-dddd-dddd-dddddddddddd");
  private static final UUID FROM_USER_ID = UUID.fromString("eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee");

  private static final TicketCategory TECHNICAL_CATEGORY =
      TicketCategory.builder().id(TECHNICAL_CATEGORY_ID).code("TICKET_CATEGORY_TECHNICAL").build();

  private static final TicketCategory CLAIM_CATEGORY =
      TicketCategory.builder().id(CLAIM_CATEGORY_ID).code("TICKET_CATEGORY_CLAIM").build();

  private static final BuzzmaUser ADMIN_USER =
      BuzzmaUser.builder().id(ADMIN_ID).role(UserRole.ROLE_ADMIN).build();

  private static final Connection ACCEPTED_CONNECTION =
      Connection.builder().fromUserId(FROM_USER_ID).toUserId(RAISER_ID).build();

  @Mock private TicketCategoryService mockTicketCategoryService;
  @Mock private ConnectionService mockConnectionService;
  @Mock private UserService mockUserService;
  private SimpleTicketRouter routingStrategy;

  @BeforeEach
  void setUp() {
    this.routingStrategy =
        new SimpleTicketRouter(
            this.mockTicketCategoryService, this.mockConnectionService, this.mockUserService);
  }

  @Test
  void route_technicalCategory_assignsAdminUser() {
    final Ticket ticket =
        Ticket.builder().categoryId(TECHNICAL_CATEGORY_ID).raisedBy(RAISER_ID).build();
    when(this.mockTicketCategoryService.getById(TECHNICAL_CATEGORY_ID))
        .thenReturn(TECHNICAL_CATEGORY);
    when(this.mockUserService.getByRole(UserRole.ROLE_ADMIN)).thenReturn(ADMIN_USER);

    final Ticket routed = this.routingStrategy.route(ticket);

    assertEquals(ADMIN_ID, routed.getAssigneeId());
  }

  @Test
  void route_nonTechnicalCategory_assignsConnectionFromUser() {
    final Ticket ticket =
        Ticket.builder().categoryId(CLAIM_CATEGORY_ID).raisedBy(RAISER_ID).build();
    when(this.mockTicketCategoryService.getById(CLAIM_CATEGORY_ID)).thenReturn(CLAIM_CATEGORY);
    when(this.mockConnectionService.getConnectionByToUserIdAndStatus(
            RAISER_ID, ConnectionStatus.CONNECTION_STATUS_ACCEPTED))
        .thenReturn(ACCEPTED_CONNECTION);

    final Ticket routed = this.routingStrategy.route(ticket);

    assertEquals(FROM_USER_ID, routed.getAssigneeId());
  }
}
