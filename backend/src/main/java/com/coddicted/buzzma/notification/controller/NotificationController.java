package com.coddicted.buzzma.notification.controller;

import com.coddicted.buzzma.notification.dto.PagedNotificationsResponseDto;
import com.coddicted.buzzma.notification.entity.Notification;
import com.coddicted.buzzma.notification.entity.NotificationStatus;
import com.coddicted.buzzma.notification.mapper.NotificationMapper;
import com.coddicted.buzzma.notification.service.NotificationService;
import com.coddicted.buzzma.shared.security.CurrentUserId;
import java.util.Map;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
  public PagedNotificationsResponseDto list(
      @CurrentUserId final UUID requesterId,
      @RequestParam final NotificationStatus status,
      @RequestParam(defaultValue = "0") final int page,
      @RequestParam(defaultValue = "10") final int size) {
    final Page<Notification> notificationsPage =
        this.notificationService.listByUserId(requesterId, status, page, size);
    return PagedNotificationsResponseDto.builder()
        .items(this.notificationMapper.toResponses(notificationsPage.getContent()))
        .total(notificationsPage.getTotalElements())
        .page(page)
        .totalPages(notificationsPage.getTotalPages())
        .build();
  }

  @GetMapping("/unread-count")
  public Map<String, Long> unreadCount(@CurrentUserId final UUID requesterId) {
    return Map.of("unread", this.notificationService.countUnread(requesterId));
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
