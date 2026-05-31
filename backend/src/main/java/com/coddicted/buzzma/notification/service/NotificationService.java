package com.coddicted.buzzma.notification.service;

import com.coddicted.buzzma.notification.entity.Notification;
import java.util.List;
import java.util.UUID;

public interface NotificationService {
  List<Notification> create(String title, String message, List<UUID> userId, UUID requesterId);

  Notification create(String title, String message, UUID userId, UUID requesterId);

  List<Notification> listByUserId(UUID userId);

  void markAsRead(UUID notificationId, UUID requesterId);

  void pinNotification(UUID notificationId, UUID requesterId);

  void markAsUnread(UUID notificationId, UUID requesterId);

  void markAllRead(UUID requesterId);

  void markAllUnread(UUID requesterId);
}
