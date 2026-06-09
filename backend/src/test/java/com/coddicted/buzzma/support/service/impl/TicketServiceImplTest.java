package com.coddicted.buzzma.support.service.impl;

import static com.coddicted.buzzma.support.entity.TicketStatus.TICKET_STATUS_IN_PROGRESS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.Mockito.when;

import com.coddicted.buzzma.shared.exception.InvalidStateTransitionException;
import com.coddicted.buzzma.support.entity.Ticket;
import com.coddicted.buzzma.support.entity.TicketStatus;
import com.coddicted.buzzma.support.entity.TicketSubCategory;
import com.coddicted.buzzma.support.notification.TicketEventPublisher;
import com.coddicted.buzzma.support.persistence.TicketRepository;
import com.coddicted.buzzma.support.router.TicketRouter;
import com.coddicted.buzzma.support.service.TicketCategoryService;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class TicketServiceImplTest {

  private static final UUID TICKET_ID = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
  private static final UUID USER_ID = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
  private static final UUID ASSIGNEE_ID = UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc");
  private static final UUID CATEGORY_ID = UUID.fromString("dddddddd-dddd-dddd-dddd-dddddddddddd");
  private static final UUID SUB_CATEGORY_ID =
      UUID.fromString("eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee");

  private static final Ticket ASSIGNED_TICKET =
      Ticket.builder()
          .id(TICKET_ID)
          .raisedBy(USER_ID)
          .assigneeId(ASSIGNEE_ID)
          .status(TICKET_STATUS_IN_PROGRESS)
          .isDeleted(false)
          .build();

  private static final TicketSubCategory SUB_CATEGORY =
      TicketSubCategory.builder()
          .id(SUB_CATEGORY_ID)
          .categoryId(CATEGORY_ID)
          .code("TICKET_SUB_CATEGORY_GENERAL")
          .metadata(null)
          .build();

  @Mock private TicketRepository mockTicketRepository;
  @Mock private TicketCategoryService mockTicketCategoryService;
  @Mock private TicketRouter mockTicketRouter;
  @Mock private TicketEventPublisher mockTicketEventPublisher;
  private TicketServiceImpl ticketService;

  @BeforeEach
  void setUp() {
    this.ticketService =
        new TicketServiceImpl(
            this.mockTicketRepository,
            this.mockTicketCategoryService,
            new TicketStateMachine(),
            this.mockTicketRouter,
            this.mockTicketEventPublisher);
  }

  @Test
  void create_resolvesAssigneeViaRoutingStrategyAndSavesAsAssigned() {
    final Ticket input =
        Ticket.builder()
            .categoryId(CATEGORY_ID)
            .subCategoryId(SUB_CATEGORY_ID)
            .description("help")
            .build();
    when(this.mockTicketCategoryService.getTicketSubCategory(CATEGORY_ID, SUB_CATEGORY_ID))
        .thenReturn(SUB_CATEGORY);
    when(this.mockTicketRouter.route(
            org.mockito.ArgumentMatchers.argThat(t -> USER_ID.equals(t.getRaisedBy()))))
        .thenReturn(ASSIGNED_TICKET);
    final ArgumentCaptor<Ticket> captor = forClass(Ticket.class);
    when(this.mockTicketRepository.save(captor.capture())).thenReturn(ASSIGNED_TICKET);

    this.ticketService.create(input, USER_ID);

    final Ticket saved = captor.getValue();
    assertEquals(TICKET_STATUS_IN_PROGRESS, saved.getStatus());
    assertEquals(ASSIGNEE_ID, saved.getAssigneeId());
    assertEquals(USER_ID, saved.getRaisedBy());
  }

  @Test
  void listByRaisedBy_returnsPageFromRepository() {
    final Pageable pageable = PageRequest.of(0, 20);
    final Page<Ticket> expected = new PageImpl<>(List.of(ASSIGNED_TICKET));
    when(this.mockTicketRepository.findAllByRaisedByAndIsDeletedFalse(USER_ID, pageable))
        .thenReturn(expected);

    assertEquals(expected, this.ticketService.listByRaisedBy(USER_ID, pageable));
  }

  @Test
  void listByAssigneeId_returnsPageFromRepository() {
    final Pageable pageable = PageRequest.of(0, 20);
    final Page<Ticket> expected = new PageImpl<>(List.of(ASSIGNED_TICKET));
    when(this.mockTicketRepository.findAllByAssigneeIdAndIsDeletedFalse(ASSIGNEE_ID, pageable))
        .thenReturn(expected);

    assertEquals(expected, this.ticketService.listByAssigneeId(ASSIGNEE_ID, pageable));
  }

  @Test
  void assign_transitionsStatusAndPersists() {
    final Ticket openTicket =
        ASSIGNED_TICKET.toBuilder()
            .assigneeId(null)
            .status(TicketStatus.TICKET_STATUS_OPEN)
            .build();
    when(this.mockTicketRepository.findById(TICKET_ID)).thenReturn(Optional.of(openTicket));
    final ArgumentCaptor<Ticket> captor = forClass(Ticket.class);
    when(this.mockTicketRepository.save(captor.capture())).thenReturn(ASSIGNED_TICKET);

    this.ticketService.assign(TICKET_ID, ASSIGNEE_ID, USER_ID);

    final Ticket saved = captor.getValue();
    assertEquals(TICKET_STATUS_IN_PROGRESS, saved.getStatus());
    assertEquals(ASSIGNEE_ID, saved.getAssigneeId());
  }

  @Test
  void close_transitionsToClosedAndPersists() {
    when(this.mockTicketRepository.findById(TICKET_ID)).thenReturn(Optional.of(ASSIGNED_TICKET));
    final ArgumentCaptor<Ticket> captor = forClass(Ticket.class);
    when(this.mockTicketRepository.save(captor.capture())).thenReturn(ASSIGNED_TICKET);

    this.ticketService.close(TICKET_ID, USER_ID);

    assertEquals(TicketStatus.TICKET_STATUS_CLOSED, captor.getValue().getStatus());
  }

  @Test
  void delete_softDeletesTicket() {
    when(this.mockTicketRepository.findById(TICKET_ID)).thenReturn(Optional.of(ASSIGNED_TICKET));
    final ArgumentCaptor<Ticket> captor = forClass(Ticket.class);
    when(this.mockTicketRepository.save(captor.capture())).thenReturn(ASSIGNED_TICKET);

    this.ticketService.delete(TICKET_ID, USER_ID);

    assertEquals(Boolean.TRUE, captor.getValue().getIsDeleted());
  }

  @Test
  void close_throwsWhenAlreadyClosed() {
    final Ticket closedTicket =
        ASSIGNED_TICKET.toBuilder().status(TicketStatus.TICKET_STATUS_CLOSED).build();
    when(this.mockTicketRepository.findById(TICKET_ID)).thenReturn(Optional.of(closedTicket));

    assertThrows(
        InvalidStateTransitionException.class, () -> this.ticketService.close(TICKET_ID, USER_ID));
  }
}
