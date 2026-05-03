package com.coddicted.buzzma.support.service;

import com.coddicted.buzzma.support.api.TicketCommentRequestDto;
import com.coddicted.buzzma.support.api.TicketCommentResponseDto;
import java.util.List;
import java.util.UUID;

public interface TicketCommentService {

  List<TicketCommentResponseDto> list(int limit, int offset);

  TicketCommentResponseDto getById(UUID id);

  TicketCommentResponseDto create(TicketCommentRequestDto request);

  TicketCommentResponseDto update(UUID id, TicketCommentRequestDto request);

  void delete(UUID id);
}
