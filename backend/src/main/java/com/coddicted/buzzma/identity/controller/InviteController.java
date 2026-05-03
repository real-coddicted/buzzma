package com.coddicted.buzzma.identity.controller;

import com.coddicted.buzzma.identity.api.ConsumeInviteRequestDto;
import com.coddicted.buzzma.identity.api.InviteRequestDto;
import com.coddicted.buzzma.identity.api.InviteResponseDto;
import com.coddicted.buzzma.identity.entity.Invite;
import com.coddicted.buzzma.identity.entity.UserRole;
import com.coddicted.buzzma.identity.mapper.InvitesMapper;
import com.coddicted.buzzma.identity.service.InviteService;
import com.coddicted.buzzma.shared.security.CurrentUserId;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/invites")
@Validated
public class InviteController {

  private final InviteService service;
  private final InvitesMapper mapper;

  public InviteController(final InviteService service, final InvitesMapper mapper) {
    this.service = service;
    this.mapper = mapper;
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public InviteResponseDto create(
      @CurrentUserId final UUID requesterId, @Valid @RequestBody final InviteRequestDto request) {
    final Invite invite = service.create(mapper.toEntity(request), requesterId);
    return mapper.toResponse(invite);
  }

  @PostMapping("/consume")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void consume(
      @CurrentUserId final UUID requesterId,
      @Valid @RequestBody final ConsumeInviteRequestDto request) {
    final Invite invite =
        service.getByRoleAndCode(
            UserRole.valueOf(request.getInviteeRole()), request.getInviteCode());
    service.consume(invite, requesterId);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@CurrentUserId final UUID requesterId, @PathVariable final UUID id) {
    service.delete(id, requesterId);
  }
}
