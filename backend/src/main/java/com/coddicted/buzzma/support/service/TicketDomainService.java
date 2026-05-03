package com.coddicted.buzzma.support.service;

import com.coddicted.buzzma.support.api.TicketCommentResponseDto;
import com.coddicted.buzzma.support.api.TicketResponseDto;
import java.util.UUID;

public interface TicketDomainService {

  TicketResponseDto createTicket(
      UUID userId,
      String userName,
      String role,
      String orderId,
      String issueType,
      String description,
      String targetRole,
      String priority);

  TicketResponseDto resolveTicket(UUID ticketId, String note, UUID resolvedBy);

  TicketResponseDto rejectTicket(UUID ticketId, String note, UUID rejectedBy);

  TicketCommentResponseDto addComment(
      UUID ticketId, String message, UUID userId, String userName, String role);
}
