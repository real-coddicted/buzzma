package com.coddicted.buzzma.support.api;

import com.coddicted.buzzma.shared.enums.TicketStatus;

public interface SupportQueryPort {

  boolean existsOpenTicketForOrder(String orderId, TicketStatus status);
}
