package com.coddicted.buzzma.notification.controller;

import com.coddicted.buzzma.notification.dto.NotificationResponseDto;
import com.coddicted.buzzma.notification.mapper.NotificationMapper;
import com.coddicted.buzzma.notification.service.NotificationService;
import com.coddicted.buzzma.shared.security.CurrentUserId;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

  private final NotificationService notificationService;
  private final NotificationMapper notificationMapper;

  public NotificationController(
      final NotificationService notificationService, final NotificationMapper notificationMapper) {
    this.notificationService = notificationService;
    this.notificationMapper = notificationMapper;
  }

  @GetMapping
  public List<NotificationResponseDto> list(@CurrentUserId final UUID requesterId) {
    return this.notificationMapper.toResponses(this.notificationService.listByUserId(requesterId));
  }

  @PutMapping("/{id}/read")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void markAsRead(@CurrentUserId final UUID requesterId, @PathVariable final UUID id) {
    this.notificationService.markAsRead(id, requesterId);
  }

  @PutMapping("/{id}/unread")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void markAsUnread(@CurrentUserId final UUID requesterId, @PathVariable final UUID id) {
    this.notificationService.markAsUnread(id, requesterId);
  }

  @PutMapping("/{id}/pin")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void pin(@CurrentUserId final UUID requesterId, @PathVariable final UUID id) {
    this.notificationService.pinNotification(id, requesterId);
  }

  @PutMapping("/read-all")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void markAllRead(@CurrentUserId final UUID requesterId) {
    this.notificationService.markAllRead(requesterId);
  }

  @PutMapping("/unread-all")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void markAllUnread(@CurrentUserId final UUID requesterId) {
    this.notificationService.markAllUnread(requesterId);
  }
}
