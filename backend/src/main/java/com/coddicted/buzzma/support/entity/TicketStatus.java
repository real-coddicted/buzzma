package com.coddicted.buzzma.support.entity;

public enum TicketStatus {
  // created but not assigned
  TICKET_STATUS_OPEN,
  // ticket assigned and support team is working on it
  TICKET_STATUS_IN_PROGRESS,
  // need additional info from the user
  TICKET_STATUS_WAITING_FOR_USER_ACTION,
  // resolution provided
  TICKET_STATUS_RESOLVED,
  // terminal state - can be manually closed or auto closed after certain period of time
  TICKET_STATUS_CLOSED
}
