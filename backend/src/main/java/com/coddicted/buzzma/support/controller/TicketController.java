package com.coddicted.buzzma.support.controller;

import com.coddicted.buzzma.shared.security.CurrentUserId;
import com.coddicted.buzzma.support.dto.TicketAssignRequestDto;
import com.coddicted.buzzma.support.dto.TicketAttachmentResponseDto;
import com.coddicted.buzzma.support.dto.TicketCommentRequestDto;
import com.coddicted.buzzma.support.dto.TicketCommentResponseDto;
import com.coddicted.buzzma.support.dto.TicketRequestDto;
import com.coddicted.buzzma.support.dto.TicketResponseDto;
import com.coddicted.buzzma.support.dto.TicketStatusUpdateRequestDto;
import com.coddicted.buzzma.support.entity.Ticket;
import com.coddicted.buzzma.support.entity.TicketComment;
import com.coddicted.buzzma.support.mapper.TicketAttachmentMapper;
import com.coddicted.buzzma.support.mapper.TicketCommentMapper;
import com.coddicted.buzzma.support.mapper.TicketMapper;
import com.coddicted.buzzma.support.service.TicketAttachmentService;
import com.coddicted.buzzma.support.service.TicketCommentService;
import com.coddicted.buzzma.support.service.TicketService;
import jakarta.validation.Valid;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/tickets")
@Validated
public class TicketController {

  private final TicketService ticketService;
  private final TicketCommentService ticketCommentService;
  private final TicketAttachmentService ticketAttachmentService;
  private final TicketMapper ticketMapper;
  private final TicketCommentMapper ticketCommentMapper;
  private final TicketAttachmentMapper ticketAttachmentMapper;

  public TicketController(
      final TicketService ticketService,
      final TicketCommentService ticketCommentService,
      final TicketAttachmentService ticketAttachmentService,
      final TicketMapper ticketMapper,
      final TicketCommentMapper ticketCommentMapper,
      final TicketAttachmentMapper ticketAttachmentMapper) {
    this.ticketService = ticketService;
    this.ticketCommentService = ticketCommentService;
    this.ticketAttachmentService = ticketAttachmentService;
    this.ticketMapper = ticketMapper;
    this.ticketCommentMapper = ticketCommentMapper;
    this.ticketAttachmentMapper = ticketAttachmentMapper;
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public TicketResponseDto create(
      @CurrentUserId final UUID requesterId, @Valid @RequestBody final TicketRequestDto request) {
    final Ticket ticket =
        this.ticketService.create(this.ticketMapper.toEntity(request), requesterId);
    return this.ticketMapper.toResponse(ticket);
  }

  @GetMapping
  public List<TicketResponseDto> list(@CurrentUserId final UUID requesterId) {
    return this.ticketService.listByRaisedBy(requesterId).stream()
        .map(this.ticketMapper::toResponse)
        .toList();
  }

  @GetMapping("/{id}")
  public TicketResponseDto getById(
      @CurrentUserId final UUID requesterId, @PathVariable final UUID id) {
    return this.ticketMapper.toResponse(this.ticketService.getById(id));
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@CurrentUserId final UUID requesterId, @PathVariable final UUID id) {
    this.ticketService.delete(id, requesterId);
  }

  @PostMapping("/{id}/assign")
  public TicketResponseDto assign(
      @CurrentUserId final UUID requesterId,
      @PathVariable final UUID id,
      @Valid @RequestBody final TicketAssignRequestDto request) {
    return this.ticketMapper.toResponse(
        this.ticketService.assign(id, request.getAssigneeId(), requesterId));
  }

  @PatchMapping("/{id}/status")
  public TicketResponseDto updateStatus(
      @CurrentUserId final UUID requesterId,
      @PathVariable final UUID id,
      @Valid @RequestBody final TicketStatusUpdateRequestDto request) {
    return this.ticketMapper.toResponse(
        this.ticketService.updateStatus(id, request.getStatus(), requesterId));
  }

  @PostMapping("/{id}/close")
  public TicketResponseDto close(
      @CurrentUserId final UUID requesterId, @PathVariable final UUID id) {
    return this.ticketMapper.toResponse(this.ticketService.close(id, requesterId));
  }

  @PostMapping("/{id}/comments")
  @ResponseStatus(HttpStatus.CREATED)
  public TicketCommentResponseDto addComment(
      @CurrentUserId final UUID requesterId,
      @PathVariable final UUID id,
      @Valid @RequestBody final TicketCommentRequestDto request) {
    final TicketComment comment =
        this.ticketCommentService.addComment(
            this.ticketCommentMapper.toEntity(request).toBuilder().ticketId(id).build(),
            requesterId);
    return this.ticketCommentMapper.toResponse(comment);
  }

  @GetMapping("/{id}/comments")
  public List<TicketCommentResponseDto> listComments(
      @CurrentUserId final UUID requesterId, @PathVariable final UUID id) {
    return this.ticketCommentService.listByTicketId(id).stream()
        .map(this.ticketCommentMapper::toResponse)
        .toList();
  }

  @PostMapping("/{id}/attachments")
  @ResponseStatus(HttpStatus.CREATED)
  public TicketAttachmentResponseDto uploadAttachment(
      @CurrentUserId final UUID requesterId,
      @PathVariable final UUID id,
      @RequestParam("file") final MultipartFile file)
      throws IOException {
    return this.ticketAttachmentMapper.toResponse(
        this.ticketAttachmentService.upload(
            id, file.getOriginalFilename(), file.getContentType(), file.getBytes(), requesterId));
  }

  @GetMapping("/{id}/attachments")
  public List<TicketAttachmentResponseDto> listAttachments(
      @CurrentUserId final UUID requesterId, @PathVariable final UUID id) {
    return this.ticketAttachmentService.listByTicketId(id).stream()
        .map(this.ticketAttachmentMapper::toResponse)
        .toList();
  }
}
