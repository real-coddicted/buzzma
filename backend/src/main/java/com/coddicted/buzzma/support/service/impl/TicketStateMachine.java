package com.coddicted.buzzma.support.service.impl;

import static com.coddicted.buzzma.support.entity.TicketStatus.TICKET_STATUS_ASSIGNED;
import static com.coddicted.buzzma.support.entity.TicketStatus.TICKET_STATUS_CLOSED;
import static com.coddicted.buzzma.support.entity.TicketStatus.TICKET_STATUS_OPEN;
import static com.coddicted.buzzma.support.entity.TicketStatus.TICKET_STATUS_WAITING_FOR_USER_ACTION;

import com.coddicted.buzzma.shared.exception.InvalidStateTransitionException;
import com.coddicted.buzzma.support.entity.Ticket;
import com.coddicted.buzzma.support.entity.TicketStatus;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class TicketStateMachine {

  private static final Map<TicketStatus, Set<TicketStatus>> TRANSITIONS =
      new EnumMap<>(TicketStatus.class);

  static {
    TRANSITIONS.put(TICKET_STATUS_OPEN, EnumSet.of(TICKET_STATUS_ASSIGNED, TICKET_STATUS_CLOSED));
    TRANSITIONS.put(
        TICKET_STATUS_ASSIGNED,
        EnumSet.of(TICKET_STATUS_WAITING_FOR_USER_ACTION, TICKET_STATUS_CLOSED));
    TRANSITIONS.put(
        TICKET_STATUS_WAITING_FOR_USER_ACTION,
        EnumSet.of(TICKET_STATUS_ASSIGNED, TICKET_STATUS_CLOSED));
    TRANSITIONS.put(TICKET_STATUS_CLOSED, EnumSet.noneOf(TicketStatus.class));
  }

  public void transition(final Ticket ticket, final TicketStatus target) {
    final Set<TicketStatus> allowed = TRANSITIONS.get(ticket.getStatus());
    if (!allowed.contains(target)) {
      throw new InvalidStateTransitionException(
          "Cannot transition ticket from " + ticket.getStatus() + " to " + target);
    }
    ticket.setStatus(target);
  }
}
