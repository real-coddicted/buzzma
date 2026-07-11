package com.coddicted.buzzma.settings.controller;

import com.coddicted.buzzma.identity.entity.UserRole;
import com.coddicted.buzzma.settings.dto.UserSettingsDto;
import com.coddicted.buzzma.settings.entity.Settings;
import com.coddicted.buzzma.settings.entity.UserSettings;
import com.coddicted.buzzma.settings.mapper.UserSettingsMapper;
import com.coddicted.buzzma.settings.service.UserSettingsService;
import com.coddicted.buzzma.shared.security.CurrentUserId;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/user-settings")
@Validated
public class UserSettingsController {

  private final UserSettingsService userSettingsService;
  private final UserSettingsMapper userSettingsMapper;

  public UserSettingsController(
      final UserSettingsService userSettingsService, final UserSettingsMapper userSettingsMapper) {
    this.userSettingsService = userSettingsService;
    this.userSettingsMapper = userSettingsMapper;
  }

  @GetMapping
  public UserSettingsDto get(@CurrentUserId final UUID requesterId) {
    final UserSettings userSettings = this.userSettingsService.getByUserId(requesterId);
    return this.userSettingsMapper.toUserSettingsDto(userSettings);
  }

  @GetMapping("/{userId}")
  @PreAuthorize(UserRole.Expr.ADMIN)
  public UserSettingsDto get(
      @PathVariable final UUID userId, @CurrentUserId final UUID requesterId) {
    final UserSettings userSettings = this.userSettingsService.getByUserId(userId);
    return this.userSettingsMapper.toUserSettingsDto(userSettings);
  }

  @PostMapping
  public UserSettingsDto create(
      @CurrentUserId final UUID requesterId, @Valid @RequestBody final UserSettingsDto request) {
    final UserSettings userSettings = this.userSettingsMapper.toEntity(request);
    final UserSettings created = this.userSettingsService.create(userSettings, requesterId);
    return this.userSettingsMapper.toUserSettingsDto(created);
  }

  @PostMapping("/setToDefault")
  // TODO add pre-auth check for admin role
  public UserSettingsDto addDefault(
      @CurrentUserId final UUID requesterId, @RequestParam final UUID userId) {
    final UserSettings userSettings = this.userSettingsService.setToDefault(userId, requesterId);
    return this.userSettingsMapper.toUserSettingsDto(userSettings);
  }

  @PutMapping("/{userId}")
  public UserSettingsDto update(
      @CurrentUserId final UUID requesterId,
      @PathVariable final UUID userId,
      @Valid @RequestBody final UserSettingsDto request) {
    final UserSettings existing = this.userSettingsService.getByUserId(userId);
    final Settings newSettings = this.userSettingsMapper.toSettings(request);
    final UserSettings toUpdate = existing.toBuilder().settings(newSettings).build();
    final UserSettings updated = this.userSettingsService.update(toUpdate, requesterId);
    return this.userSettingsMapper.toUserSettingsDto(updated);
  }

  @DeleteMapping("/{userId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@CurrentUserId final UUID requesterId, @PathVariable final UUID userId) {
    this.userSettingsService.delete(userId, requesterId);
  }
}
