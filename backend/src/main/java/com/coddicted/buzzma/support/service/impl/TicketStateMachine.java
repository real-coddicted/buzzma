package com.coddicted.buzzma.support.service.impl;

import com.coddicted.buzzma.shared.exception.InvalidStateTransitionException;
import com.coddicted.buzzma.support.entity.Ticket;
import com.coddicted.buzzma.support.entity.TicketAction;
import com.coddicted.buzzma.support.entity.TicketStatus;
import java.util.EnumMap;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class TicketStateMachine {

  private static final Map<TicketStatus, Map<TicketAction, TicketStatus>> TRANSITIONS =
      new EnumMap<>(TicketStatus.class);

  static {
    Map<TicketAction, TicketStatus> fromOpen = new EnumMap<>(TicketAction.class);
    fromOpen.put(TicketAction.TICKET_ACTION_CLOSE, TicketStatus.TICKET_STATUS_CLOSED);
    TRANSITIONS.put(TicketStatus.TICKET_STATUS_OPEN, fromOpen);

    Map<TicketAction, TicketStatus> fromInProgress = new EnumMap<>(TicketAction.class);
    fromInProgress.put(TicketAction.TICKET_ACTION_CLOSE, TicketStatus.TICKET_STATUS_CLOSED);
    fromInProgress.put(
        TicketAction.TICKET_ACTION_MARK_RESOLVE, TicketStatus.TICKET_STATUS_RESOLVED);
    fromInProgress.put(
        TicketAction.TICKET_ACTION_REQUEST_ADDITIONAL_INFO,
        TicketStatus.TICKET_STATUS_WAITING_FOR_USER_ACTION);
    TRANSITIONS.put(TicketStatus.TICKET_STATUS_IN_PROGRESS, fromInProgress);

    Map<TicketAction, TicketStatus> fromWaiting = new EnumMap<>(TicketAction.class);
    fromWaiting.put(TicketAction.TICKET_ACTION_CLOSE, TicketStatus.TICKET_STATUS_CLOSED);
    fromWaiting.put(
        TicketAction.TICKET_ACTION_INFO_PROVIDED, TicketStatus.TICKET_STATUS_IN_PROGRESS);
    TRANSITIONS.put(TicketStatus.TICKET_STATUS_WAITING_FOR_USER_ACTION, fromWaiting);

    Map<TicketAction, TicketStatus> fromResolved = new EnumMap<>(TicketAction.class);
    fromResolved.put(TicketAction.TICKET_ACTION_CLOSE, TicketStatus.TICKET_STATUS_CLOSED);
    fromResolved.put(TicketAction.TICKET_ACTION_REOPEN, TicketStatus.TICKET_STATUS_IN_PROGRESS);
    TRANSITIONS.put(TicketStatus.TICKET_STATUS_RESOLVED, fromResolved);

    TRANSITIONS.put(TicketStatus.TICKET_STATUS_CLOSED, new EnumMap<>(TicketAction.class));
  }

  public void transition(final Ticket ticket, final TicketAction action) {
    final Map<TicketAction, TicketStatus> allowed =
        TRANSITIONS.getOrDefault(ticket.getStatus(), new EnumMap<>(TicketAction.class));
    final TicketStatus next = allowed.get(action);
    if (next == null) {
      throw new InvalidStateTransitionException(
          "Cannot apply action " + action + " to ticket in status " + ticket.getStatus());
    }
    ticket.setStatus(next);
  }
}
