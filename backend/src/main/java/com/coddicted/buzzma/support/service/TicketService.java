package com.coddicted.buzzma.support.service;

import com.coddicted.buzzma.support.entity.Ticket;
import com.coddicted.buzzma.support.entity.TicketAction;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TicketService {

  Ticket create(Ticket ticket, UUID requesterId);

  Ticket getById(UUID ticketId);

  Page<Ticket> listByRaisedBy(UUID userId, Pageable pageable);

  Page<Ticket> listByAssigneeId(UUID userId, Pageable pageable);

  Page<Ticket> listTickets(int page, int size);

  Ticket assign(UUID ticketId, UUID assigneeId, UUID requesterId);

  Ticket updateStatus(UUID ticketId, TicketAction action, UUID requesterId);

  Ticket close(UUID ticketId, UUID requesterId);

  void delete(UUID ticketId, UUID requesterId);
}
