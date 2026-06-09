package com.coddicted.buzzma.support.service.impl;

import static com.coddicted.buzzma.support.entity.TicketAction.TICKET_ACTION_CLOSE;
import static com.coddicted.buzzma.support.entity.TicketStatus.TICKET_STATUS_IN_PROGRESS;

import com.coddicted.buzzma.shared.common.BaseCrudService;
import com.coddicted.buzzma.shared.exception.BusinessRuleViolationException;
import com.coddicted.buzzma.support.entity.Ticket;
import com.coddicted.buzzma.support.entity.TicketAction;
import com.coddicted.buzzma.support.entity.TicketSubCategory;
import com.coddicted.buzzma.support.entity.TicketSubCategoryMetadata;
import com.coddicted.buzzma.support.notification.TicketEventPublisher;
import com.coddicted.buzzma.support.persistence.TicketRepository;
import com.coddicted.buzzma.support.router.TicketRouter;
import com.coddicted.buzzma.support.service.TicketCategoryService;
import com.coddicted.buzzma.support.service.TicketService;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TicketServiceImpl extends BaseCrudService implements TicketService {

  private static final Logger LOGGER = LoggerFactory.getLogger(TicketServiceImpl.class);

  private final TicketRepository ticketRepository;
  private final TicketCategoryService ticketCategoryService;
  private final TicketStateMachine stateMachine;
  private final TicketRouter ticketRouter;
  private final TicketEventPublisher ticketEventPublisher;

  public TicketServiceImpl(
      final TicketRepository ticketRepository,
      final TicketCategoryService ticketCategoryService,
      final TicketStateMachine stateMachine,
      final TicketRouter ticketRouter,
      final TicketEventPublisher ticketEventPublisher) {
    this.ticketRepository = ticketRepository;
    this.ticketCategoryService = ticketCategoryService;
    this.stateMachine = stateMachine;
    this.ticketRouter = ticketRouter;
    this.ticketEventPublisher = ticketEventPublisher;
  }

  @Override
  @Transactional
  public Ticket create(final Ticket ticket, final UUID requesterId) {
    final TicketSubCategory ticketSubCategory =
        this.ticketCategoryService.getTicketSubCategory(
            ticket.getCategoryId(), ticket.getSubCategoryId());

    final TicketSubCategoryMetadata metadata = ticketSubCategory.getMetadata();
    if (metadata != null) {
      if (metadata.isOrderIdRequired() && ticket.getOrderId() == null) {
        throw new BusinessRuleViolationException(
            "order id is required for sub-category: " + ticketSubCategory.getCode());
      }
    }

    final Ticket prepared =
        ticket.toBuilder()
            .status(TICKET_STATUS_IN_PROGRESS)
            .isDeleted(false)
            .raisedBy(requesterId)
            .createdBy(requesterId)
            .updatedBy(requesterId)
            .build();
    // assign ticket
    final Ticket assignedTicket = this.ticketRouter.route(prepared);
    // save ticket
    final Ticket saved = this.ticketRepository.save(assignedTicket);
    // publish event
    this.ticketEventPublisher.publishTicketCreatedEvent(saved);
    return saved;
  }

  @Override
  @Transactional(readOnly = true)
  public Ticket getById(final UUID ticketId) {
    return mustFind(this.ticketRepository, ticketId, "Ticket");
  }

  @Override
  @Transactional(readOnly = true)
  public Page<Ticket> listByRaisedBy(final UUID userId, final Pageable pageable) {
    return this.ticketRepository.findAllByRaisedByAndIsDeletedFalse(userId, pageable);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<Ticket> listByAssigneeId(final UUID userId, final Pageable pageable) {
    return this.ticketRepository.findAllByAssigneeIdAndIsDeletedFalse(userId, pageable);
  }

  @Override
  public Page<Ticket> listTickets(final int page, final int size) {
    return this.ticketRepository.findAllByIsDeletedFalse(PageRequest.of(page, size));
  }

  @Override
  @Transactional
  public Ticket assign(final UUID ticketId, final UUID assigneeId, final UUID requesterId) {
    final Ticket ticket = mustFind(this.ticketRepository, ticketId, "Ticket");
    final Ticket updated =
        ticket.toBuilder()
            .assigneeId(assigneeId)
            .status(TICKET_STATUS_IN_PROGRESS)
            .updatedBy(requesterId)
            .build();
    return this.ticketRepository.save(updated);
  }

  @Override
  @Transactional
  public Ticket updateStatus(
      final UUID ticketId, final TicketAction action, final UUID requesterId) {
    final Ticket ticket = mustFind(this.ticketRepository, ticketId, "Ticket");
    this.stateMachine.transition(ticket, action);
    final Ticket updated = ticket.toBuilder().updatedBy(requesterId).build();
    return this.ticketRepository.save(updated);
  }

  @Override
  @Transactional
  public Ticket close(final UUID ticketId, final UUID requesterId) {
    final Ticket ticket = mustFind(this.ticketRepository, ticketId, "Ticket");
    this.stateMachine.transition(ticket, TICKET_ACTION_CLOSE);
    final Ticket updated = ticket.toBuilder().updatedBy(requesterId).build();
    return this.ticketRepository.save(updated);
  }

  @Override
  @Transactional
  public void delete(final UUID ticketId, final UUID requesterId) {
    final Ticket ticket = mustFind(this.ticketRepository, ticketId, "Ticket");
    LOGGER.debug("Soft-deleting ticket {}", ticketId);
    this.ticketRepository.save(ticket.toBuilder().isDeleted(true).updatedBy(requesterId).build());
  }
}
