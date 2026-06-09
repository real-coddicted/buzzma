package com.coddicted.buzzma.support.router;

import com.coddicted.buzzma.support.entity.Ticket;

public interface TicketRouter {
  Ticket route(Ticket ticket);
}
