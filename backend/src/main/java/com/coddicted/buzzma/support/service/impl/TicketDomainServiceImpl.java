package com.coddicted.buzzma.support.service.impl;

import com.coddicted.buzzma.shared.enums.TicketStatus;
import com.coddicted.buzzma.shared.exception.ApiException;
import com.coddicted.buzzma.support.api.TicketCommentResponseDto;
import com.coddicted.buzzma.support.api.TicketResponseDto;
import com.coddicted.buzzma.support.mapper.TicketCommentsMapper;
import com.coddicted.buzzma.support.mapper.TicketsMapper;
import com.coddicted.buzzma.support.persistence.TicketCommentsEntity;
import com.coddicted.buzzma.support.persistence.TicketCommentsRepository;
import com.coddicted.buzzma.support.persistence.TicketsEntity;
import com.coddicted.buzzma.support.persistence.TicketsRepository;
import com.coddicted.buzzma.support.service.TicketDomainService;
import java.time.Instant;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TicketDomainServiceImpl implements TicketDomainService {

  private final TicketsRepository ticketsRepository;
  private final TicketCommentsRepository ticketCommentsRepository;
  private final TicketsMapper ticketsMapper;
  private final TicketCommentsMapper ticketCommentsMapper;

  public TicketDomainServiceImpl(
      TicketsRepository ticketsRepository,
      TicketCommentsRepository ticketCommentsRepository,
      TicketsMapper ticketsMapper,
      TicketCommentsMapper ticketCommentsMapper) {
    this.ticketsRepository = ticketsRepository;
    this.ticketCommentsRepository = ticketCommentsRepository;
    this.ticketsMapper = ticketsMapper;
    this.ticketCommentsMapper = ticketCommentsMapper;
  }

  @Override
  @Transactional
  public TicketResponseDto createTicket(
      UUID userId,
      String userName,
      String role,
      String orderId,
      String issueType,
      String description,
      String targetRole,
      String priority) {
    TicketsEntity ticket = new TicketsEntity();
    ticket.setUserId(userId);
    ticket.setUserName(userName);
    ticket.setRole(role);
    ticket.setOrderId(orderId);
    ticket.setIssueType(issueType);
    ticket.setDescription(description);
    ticket.setTargetRole(targetRole);
    ticket.setPriority(priority);
    ticket.setStatus(TicketStatus.Open);
    ticket.setCreatedBy(userId);
    ticket.setUpdatedBy(userId);
    return ticketsMapper.toResponse(ticketsRepository.save(ticket));
  }

  @Override
  @Transactional
  public TicketResponseDto resolveTicket(UUID ticketId, String note, UUID resolvedBy) {
    TicketsEntity ticket = loadActiveTicket(ticketId);
    ticket.setStatus(TicketStatus.Resolved);
    ticket.setResolutionNote(note);
    ticket.setResolvedBy(resolvedBy);
    ticket.setResolvedAt(Instant.now());
    ticket.setUpdatedBy(resolvedBy);
    return ticketsMapper.toResponse(ticketsRepository.save(ticket));
  }

  @Override
  @Transactional
  public TicketResponseDto rejectTicket(UUID ticketId, String note, UUID rejectedBy) {
    TicketsEntity ticket = loadActiveTicket(ticketId);
    ticket.setStatus(TicketStatus.Rejected);
    ticket.setResolutionNote(note);
    ticket.setResolvedBy(rejectedBy);
    ticket.setResolvedAt(Instant.now());
    ticket.setUpdatedBy(rejectedBy);
    return ticketsMapper.toResponse(ticketsRepository.save(ticket));
  }

  @Override
  @Transactional
  public TicketCommentResponseDto addComment(
      UUID ticketId, String message, UUID userId, String userName, String role) {
    ticketsRepository
        .findById(ticketId)
        .filter(t -> !Boolean.TRUE.equals(t.getIsDeleted()))
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "TICKET_NOT_FOUND"));

    TicketCommentsEntity comment = new TicketCommentsEntity();
    comment.setTicketId(ticketId);
    comment.setUserId(userId);
    comment.setUserName(userName);
    comment.setRole(role);
    comment.setMessage(message);
    return ticketCommentsMapper.toResponse(ticketCommentsRepository.save(comment));
  }

  private TicketsEntity loadActiveTicket(UUID ticketId) {
    return ticketsRepository
        .findById(ticketId)
        .filter(t -> !Boolean.TRUE.equals(t.getIsDeleted()))
        .filter(t -> t.getStatus() == TicketStatus.Open)
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "TICKET_NOT_FOUND_OR_CLOSED"));
  }
}
