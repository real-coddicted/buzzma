package com.coddicted.buzzma.connection.controller;

import com.coddicted.buzzma.connection.dto.ConnectionRequestDto;
import com.coddicted.buzzma.connection.dto.ConnectionResponseDto;
import com.coddicted.buzzma.connection.entity.Connection;
import com.coddicted.buzzma.connection.entity.ConnectionStatus;
import com.coddicted.buzzma.connection.mapper.ConnectionMapper;
import com.coddicted.buzzma.connection.service.ConnectionService;
import com.coddicted.buzzma.shared.security.CurrentUserId;
import jakarta.validation.Valid;
import java.util.Set;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/connections")
public class ConnectionController {

  private final ConnectionService connectionService;
  private final ConnectionMapper connectionMapper;

  public ConnectionController(
      final ConnectionService connectionService, final ConnectionMapper connectionMapper) {
    this.connectionService = connectionService;
    this.connectionMapper = connectionMapper;
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public ConnectionResponseDto create(
      @CurrentUserId final UUID requesterId,
      @Valid @RequestBody final ConnectionRequestDto request) {
    final Connection connection =
        this.connectionService.createConnection(
            this.connectionMapper.toEntity(request, requesterId));
    return this.connectionMapper.toResponse(connection);
  }

  @GetMapping
  public Set<ConnectionResponseDto> list(
      @CurrentUserId final UUID requesterId, @RequestParam final ConnectionStatus status) {
    return this.connectionMapper.toResponses(
        this.connectionService.getConnectionsByFromUserIdAndStatus(requesterId, status));
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@CurrentUserId final UUID requesterId, @PathVariable final UUID id) {
    this.connectionService.delete(id, requesterId);
  }
}
