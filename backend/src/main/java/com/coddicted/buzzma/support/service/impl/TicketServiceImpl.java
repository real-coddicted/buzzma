package com.coddicted.buzzma.support.service.impl;

import static com.coddicted.buzzma.support.entity.TicketStatus.TICKET_STATUS_ASSIGNED;
import static com.coddicted.buzzma.support.entity.TicketStatus.TICKET_STATUS_CLOSED;
import static com.coddicted.buzzma.support.entity.TicketStatus.TICKET_STATUS_OPEN;

import com.coddicted.buzzma.shared.common.BaseCrudService;
import com.coddicted.buzzma.shared.exception.BusinessRuleViolationException;
import com.coddicted.buzzma.support.entity.Ticket;
import com.coddicted.buzzma.support.entity.TicketStatus;
import com.coddicted.buzzma.support.entity.TicketSubCategory;
import com.coddicted.buzzma.support.entity.TicketSubCategoryMetadata;
import com.coddicted.buzzma.support.persistence.TicketRepository;
import com.coddicted.buzzma.support.service.TicketCategoryService;
import com.coddicted.buzzma.support.service.TicketService;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TicketServiceImpl extends BaseCrudService implements TicketService {

  private static final Logger LOGGER = LoggerFactory.getLogger(TicketServiceImpl.class);

  private final TicketRepository ticketRepository;
  private final TicketCategoryService ticketCategoryService;
  private final TicketStateMachine stateMachine;

  public TicketServiceImpl(
      final TicketRepository ticketRepository,
      final TicketCategoryService ticketCategoryService,
      final TicketStateMachine stateMachine) {
    this.ticketRepository = ticketRepository;
    this.ticketCategoryService = ticketCategoryService;
    this.stateMachine = stateMachine;
  }

  @Override
  @Transactional
  public Ticket create(final Ticket ticket, final UUID requesterId) {
    final TicketSubCategory ticketSubCategory =
        this.ticketCategoryService.getTicketSubCategory(
            ticket.getSubCategoryId(), ticket.getCategoryId());

    final TicketSubCategoryMetadata metadata = ticketSubCategory.getMetadata();
    if (metadata != null) {
      if (metadata.isOrderIdRequired() && ticket.getOrderId() == null) {
        throw new BusinessRuleViolationException(
            "order id is required for sub-category: " + ticketSubCategory.getCode());
      }
    }

    final Ticket toSave =
        ticket.toBuilder()
            .status(TICKET_STATUS_OPEN)
            .isDeleted(false)
            .createdBy(requesterId)
            .updatedBy(requesterId)
            .build();
    return this.ticketRepository.save(toSave);
  }

  @Override
  @Transactional(readOnly = true)
  public Ticket getById(final UUID ticketId) {
    return mustFind(this.ticketRepository, ticketId, "Ticket");
  }

  @Override
  @Transactional(readOnly = true)
  public List<Ticket> listByRaisedBy(final UUID userId) {
    return this.ticketRepository.findAllByRaisedByAndIsDeletedFalse(userId);
  }

  @Override
  @Transactional
  public Ticket assign(final UUID ticketId, final UUID assigneeId, final UUID requesterId) {
    final Ticket ticket = mustFind(this.ticketRepository, ticketId, "Ticket");
    this.stateMachine.transition(ticket, TICKET_STATUS_ASSIGNED);
    final Ticket updated = ticket.toBuilder().assigneeId(assigneeId).updatedBy(requesterId).build();
    return this.ticketRepository.save(updated);
  }

  @Override
  @Transactional
  public Ticket updateStatus(
      final UUID ticketId, final TicketStatus status, final UUID requesterId) {
    final Ticket ticket = mustFind(this.ticketRepository, ticketId, "Ticket");
    this.stateMachine.transition(ticket, status);
    final Ticket updated = ticket.toBuilder().updatedBy(requesterId).build();
    return this.ticketRepository.save(updated);
  }

  @Override
  @Transactional
  public Ticket close(final UUID ticketId, final UUID requesterId) {
    final Ticket ticket = mustFind(this.ticketRepository, ticketId, "Ticket");
    this.stateMachine.transition(ticket, TICKET_STATUS_CLOSED);
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
