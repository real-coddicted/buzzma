package com.coddicted.buzzma.support.web;

import com.coddicted.buzzma.identity.entity.BuzzmaUser;
import com.coddicted.buzzma.identity.persistence.UsersRepository;
import com.coddicted.buzzma.shared.enums.TicketStatus;
import com.coddicted.buzzma.shared.exception.ApiException;
import com.coddicted.buzzma.shared.security.CurrentUserId;
import com.coddicted.buzzma.support.api.TicketCommentsResponseDto;
import com.coddicted.buzzma.support.api.TicketsRequestDto;
import com.coddicted.buzzma.support.api.TicketsResponseDto;
import com.coddicted.buzzma.support.persistence.TicketCommentsRepository;
import com.coddicted.buzzma.support.service.TicketDomainService;
import com.coddicted.buzzma.support.service.TicketService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
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

@RestController
@RequestMapping("/api/tickets")
@Validated
public class TicketsController {

  private final TicketService service;
  private final TicketDomainService ticketDomainService;
  private final UsersRepository usersRepository;
  private final TicketCommentsRepository ticketCommentsRepository;

  public TicketsController(
      TicketService service,
      TicketDomainService ticketDomainService,
      UsersRepository usersRepository,
      TicketCommentsRepository ticketCommentsRepository) {
    this.service = service;
    this.ticketDomainService = ticketDomainService;
    this.usersRepository = usersRepository;
    this.ticketCommentsRepository = ticketCommentsRepository;
  }

  @GetMapping
  public List<TicketsResponseDto> list(
      @RequestParam(defaultValue = "50") @Min(1) @Max(500) int limit,
      @RequestParam(defaultValue = "0") @Min(0) int offset) {
    return service.list(limit, offset);
  }

  @GetMapping("/{id}")
  public TicketsResponseDto getById(@PathVariable UUID id) {
    return service.getById(id);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public TicketsResponseDto create(@Valid @RequestBody TicketsRequestDto request) {
    return service.create(request);
  }

  @PatchMapping("/{id}")
  @PreAuthorize("isAuthenticated()")
  public TicketsResponseDto update(
      @PathVariable UUID id,
      @RequestBody UpdateTicketRequest request,
      @CurrentUserId UUID actorId) {
    String status = request.status();
    Boolean escalate = request.escalate();

    if (Boolean.TRUE.equals(escalate)) {
      return ticketDomainService.resolveTicket(id, null, actorId);
    }

    if (status == null) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "STATUS_REQUIRED");
    }

    try {
      TicketStatus ts = TicketStatus.valueOf(status);
      if (ts == TicketStatus.Resolved) {
        return ticketDomainService.resolveTicket(id, request.resolutionNote(), actorId);
      } else if (ts == TicketStatus.Rejected) {
        return ticketDomainService.rejectTicket(id, request.resolutionNote(), actorId);
      }
    } catch (IllegalArgumentException ignored) {
      // fall through to generic update
    }

    TicketsRequestDto dto =
        TicketsRequestDto.builder().status(status).resolutionNote(request.resolutionNote()).build();
    return service.update(id, dto);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable UUID id) {
    service.delete(id);
  }

  @PostMapping("/{id}/resolve")
  @PreAuthorize("isAuthenticated()")
  public TicketsResponseDto resolveTicket(
      @PathVariable UUID id, @RequestBody Map<String, String> body, @CurrentUserId UUID actorId) {
    return ticketDomainService.resolveTicket(id, body.get("note"), actorId);
  }

  @PostMapping("/{id}/reject")
  @PreAuthorize("isAuthenticated()")
  public TicketsResponseDto rejectTicket(
      @PathVariable UUID id, @RequestBody Map<String, String> body, @CurrentUserId UUID actorId) {
    return ticketDomainService.rejectTicket(id, body.get("note"), actorId);
  }

  @GetMapping("/{id}/comments")
  @PreAuthorize("isAuthenticated()")
  public Map<String, Object> getComments(@PathVariable UUID id) {
    var pageable = PageRequest.of(0, 200, Sort.by(Sort.Direction.ASC, "createdAt"));
    var comments =
        ticketCommentsRepository.findAllByTicketIdAndIsDeletedFalse(id, pageable).stream()
            .map(
                c ->
                    Map.of(
                        "id", c.getId(),
                        "userId", c.getUserId() != null ? c.getUserId() : "",
                        "userName", c.getUserName() != null ? c.getUserName() : "",
                        "role", c.getRole() != null ? c.getRole() : "",
                        "message", c.getMessage() != null ? c.getMessage() : "",
                        "createdAt", c.getCreatedAt() != null ? c.getCreatedAt() : ""))
            .toList();
    return Map.of("comments", comments);
  }

  @PostMapping("/{id}/comments")
  @ResponseStatus(HttpStatus.CREATED)
  @PreAuthorize("isAuthenticated()")
  public TicketCommentsResponseDto addComment(
      @PathVariable UUID id,
      @Valid @RequestBody AddCommentRequest request,
      @CurrentUserId UUID actorId) {
    BuzzmaUser actor =
        usersRepository
            .findById(actorId)
            .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "USER_NOT_FOUND"));
    String userName = actor.getName() != null ? actor.getName() : "User";
    String role = actor.getRole() != null ? actor.getRole().name() : "shopper";
    return ticketDomainService.addComment(id, request.message(), actorId, userName, role);
  }

  public record AddCommentRequest(@NotBlank @Size(max = 2000) String message) {}

  public record UpdateTicketRequest(String status, Boolean escalate, String resolutionNote) {}
}
