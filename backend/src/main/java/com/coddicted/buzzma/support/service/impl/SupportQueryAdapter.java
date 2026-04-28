package com.coddicted.buzzma.support.service.impl;

import com.coddicted.buzzma.shared.enums.TicketStatus;
import com.coddicted.buzzma.support.api.SupportQueryPort;
import com.coddicted.buzzma.support.persistence.TicketsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SupportQueryAdapter implements SupportQueryPort {

  private final TicketsRepository ticketsRepository;

  public SupportQueryAdapter(TicketsRepository ticketsRepository) {
    this.ticketsRepository = ticketsRepository;
  }

  @Override
  @Transactional(readOnly = true)
  public boolean existsOpenTicketForOrder(String orderId, TicketStatus status) {
    return ticketsRepository.existsByOrderIdAndStatusAndIsDeletedFalse(orderId, status);
  }
}
