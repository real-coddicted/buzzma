package com.coddicted.buzzma.support.service;

import com.coddicted.buzzma.support.entity.Ticket;
import com.coddicted.buzzma.support.entity.TicketStatus;
import java.util.List;
import java.util.UUID;

public interface TicketService {

  Ticket create(Ticket ticket, UUID requesterId);

  Ticket getById(UUID ticketId);

  List<Ticket> listByRaisedBy(UUID userId);

  Ticket assign(UUID ticketId, UUID assigneeId, UUID requesterId);

  Ticket updateStatus(UUID ticketId, TicketStatus status, UUID requesterId);

  Ticket close(UUID ticketId, UUID requesterId);

  void delete(UUID ticketId, UUID requesterId);
}
