package com.coddicted.buzzma.support.controller;

import com.coddicted.buzzma.identity.entity.BuzzmaUser;
import com.coddicted.buzzma.identity.service.UserService;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
  private final UserService userService;

  public TicketController(
      final TicketService ticketService,
      final TicketCommentService ticketCommentService,
      final TicketAttachmentService ticketAttachmentService,
      final TicketMapper ticketMapper,
      final TicketCommentMapper ticketCommentMapper,
      final TicketAttachmentMapper ticketAttachmentMapper,
      final UserService userService) {
    this.ticketService = ticketService;
    this.ticketCommentService = ticketCommentService;
    this.ticketAttachmentService = ticketAttachmentService;
    this.ticketMapper = ticketMapper;
    this.ticketCommentMapper = ticketCommentMapper;
    this.ticketAttachmentMapper = ticketAttachmentMapper;
    this.userService = userService;
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public TicketResponseDto create(
      @CurrentUserId final UUID requesterId, @Valid @RequestBody final TicketRequestDto request) {
    final Ticket ticket =
        this.ticketService.create(this.ticketMapper.toEntity(request), requesterId);
    return toResponseWithNames(ticket);
  }

  @GetMapping("/raised")
  public List<TicketResponseDto> listTicketsRaisedByUser(
      @CurrentUserId final UUID requesterId,
      @RequestParam(defaultValue = "0") final int page,
      @RequestParam(defaultValue = "20") final int size) {
    final Pageable pageable = PageRequest.of(page, size);
    final Page<Ticket> tickets = this.ticketService.listByRaisedBy(requesterId, pageable);
    final Map<UUID, String> nameMap = buildNameMap(tickets);
    return tickets.stream().map(t -> toResponseWithNames(t, nameMap)).toList();
  }

  @GetMapping("/assigned")
  public List<TicketResponseDto> listTicketsAssignedToUser(
      @CurrentUserId final UUID requesterId,
      @RequestParam(defaultValue = "0") final int page,
      @RequestParam(defaultValue = "20") final int size) {
    final Pageable pageable = PageRequest.of(page, size);
    final Page<Ticket> tickets = this.ticketService.listByAssigneeId(requesterId, pageable);
    final Map<UUID, String> nameMap = buildNameMap(tickets);
    return tickets.stream().map(t -> toResponseWithNames(t, nameMap)).toList();
  }

  @GetMapping("/{id}")
  public TicketResponseDto getById(
      @CurrentUserId final UUID requesterId, @PathVariable final UUID id) {
    return toResponseWithNames(this.ticketService.getById(id));
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
    return toResponseWithNames(this.ticketService.assign(id, request.getAssigneeId(), requesterId));
  }

  @PatchMapping("/{id}/status")
  public TicketResponseDto updateStatus(
      @CurrentUserId final UUID requesterId,
      @PathVariable final UUID id,
      @Valid @RequestBody final TicketStatusUpdateRequestDto request) {
    return toResponseWithNames(
        this.ticketService.updateStatus(id, request.getAction(), requesterId));
  }

  @PostMapping("/{id}/close")
  public TicketResponseDto close(
      @CurrentUserId final UUID requesterId, @PathVariable final UUID id) {
    return toResponseWithNames(this.ticketService.close(id, requesterId));
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
    return toCommentResponseWithName(comment);
  }

  @GetMapping("/{id}/comments")
  public List<TicketCommentResponseDto> listComments(
      @CurrentUserId final UUID requesterId, @PathVariable final UUID id) {
    final List<TicketComment> comments = this.ticketCommentService.listByTicketId(id);
    final Map<UUID, String> nameMap = buildCommentNameMap(comments);
    return comments.stream().map(c -> toCommentResponseWithName(c, nameMap)).toList();
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

  private Map<UUID, String> buildNameMap(final Page<Ticket> tickets) {
    return buildNameMap(tickets.getContent());
  }

  private TicketResponseDto toResponseWithNames(final Ticket ticket) {
    return toResponseWithNames(ticket, buildNameMap(List.of(ticket)));
  }

  private Map<UUID, String> buildNameMap(final List<Ticket> tickets) {
    final List<UUID> ids = new ArrayList<>();
    for (final Ticket t : tickets) {
      ids.add(t.getRaisedBy());
      if (t.getAssigneeId() != null) {
        ids.add(t.getAssigneeId());
      }
    }
    return this.userService.getByIds(ids).stream()
        .collect(Collectors.toMap(BuzzmaUser::getId, BuzzmaUser::getName));
  }

  private TicketResponseDto toResponseWithNames(
      final Ticket ticket, final Map<UUID, String> nameMap) {
    final String raisedByName = nameMap.get(ticket.getRaisedBy());
    final String assigneeName =
        ticket.getAssigneeId() != null ? nameMap.get(ticket.getAssigneeId()) : null;
    return this.ticketMapper.toResponse(ticket, raisedByName, assigneeName);
  }

  private Map<UUID, String> buildCommentNameMap(final List<TicketComment> comments) {
    final List<UUID> ids = comments.stream().map(TicketComment::getAuthorId).toList();
    return this.userService.getByIds(ids).stream()
        .collect(Collectors.toMap(BuzzmaUser::getId, BuzzmaUser::getName));
  }

  private TicketCommentResponseDto toCommentResponseWithName(final TicketComment comment) {
    return toCommentResponseWithName(comment, buildCommentNameMap(List.of(comment)));
  }

  private TicketCommentResponseDto toCommentResponseWithName(
      final TicketComment comment, final Map<UUID, String> nameMap) {
    return this.ticketCommentMapper.toResponse(comment, nameMap.get(comment.getAuthorId()));
  }
}
