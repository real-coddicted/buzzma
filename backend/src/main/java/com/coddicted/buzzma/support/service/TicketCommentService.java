package com.coddicted.buzzma.support.service;

import com.coddicted.buzzma.support.api.TicketCommentsRequestDto;
import com.coddicted.buzzma.support.api.TicketCommentsResponseDto;
import java.util.List;
import java.util.UUID;

public interface TicketCommentService {

  List<TicketCommentsResponseDto> list(int limit, int offset);

  TicketCommentsResponseDto getById(UUID id);

  TicketCommentsResponseDto create(TicketCommentsRequestDto request);

  TicketCommentsResponseDto update(UUID id, TicketCommentsRequestDto request);

  void delete(UUID id);
}
