package com.coddicted.buzzma.support.service;

import com.coddicted.buzzma.support.api.TicketRequestDto;
import com.coddicted.buzzma.support.api.TicketResponseDto;
import java.util.List;
import java.util.UUID;

public interface TicketService {

  List<TicketResponseDto> list(int limit, int offset);

  TicketResponseDto getById(UUID id);

  TicketResponseDto create(TicketRequestDto request);

  TicketResponseDto update(UUID id, TicketRequestDto request);

  void delete(UUID id);
}
