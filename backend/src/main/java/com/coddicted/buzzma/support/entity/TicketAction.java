package com.coddicted.buzzma.support.entity;

public enum TicketAction {
  // when the ticket is created it is assigned and status will be open
  // the reporter can close the ticket at any stage
  // once the ticket is closed no further action can be taken,
  // a ticket can be manually close or can automatically close after no activity for certain period
  // of time
  // this will transition the ticket to CLOSED status
  TICKET_ACTION_CLOSE,
  // only assignee can mark the ticket as resolved, this will transition the ticket to RESOLVED
  // status
  TICKET_ACTION_MARK_RESOLVE,
  // assignee can request for additional info from the user, this will transition the ticket to
  // WAITING_FOR_USER_ACTION status
  TICKET_ACTION_REQUEST_ADDITIONAL_INFO,
  // reporter can provide the additional info, and has to mark the ticket as INFO_PROVIDED, this
  // will transition the ticket to IN_PROGRESS status
  TICKET_ACTION_INFO_PROVIDED,
  // the ticket in resolved status can be reopened by the reporter, this will transition the ticket
  // to IN_PROGRESS status
  TICKET_ACTION_REOPEN,
}
