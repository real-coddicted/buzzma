package com.coddicted.buzzma.identity.controller;

import com.coddicted.buzzma.identity.api.UsersRequestDto;
import com.coddicted.buzzma.identity.api.UsersResponseDto;
import com.coddicted.buzzma.identity.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
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

@RestController
@RequestMapping("/api/users")
@Validated
public class UsersController {

  private final UserService service;

  public UsersController(UserService service) {
    this.service = service;
  }

  @GetMapping
  public List<UsersResponseDto> list(
      @RequestParam(defaultValue = "50") @Min(1) @Max(500) int limit,
      @RequestParam(defaultValue = "0") @Min(0) int offset) {
    return service.list(limit, offset);
  }

  @GetMapping("/{id}")
  public UsersResponseDto getById(@PathVariable UUID id) {
    return service.getById(id);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public UsersResponseDto create(@Valid @RequestBody UsersRequestDto request) {
    return service.create(request);
  }

  @PatchMapping("/{id}")
  public UsersResponseDto update(
      @PathVariable UUID id, @Valid @RequestBody UsersRequestDto request) {
    return service.update(id, request);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable UUID id) {
    service.delete(id);
  }
}
