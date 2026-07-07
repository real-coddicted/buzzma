package com.coddicted.buzzma.notification.service.impl;

import com.coddicted.buzzma.notification.entity.Notification;
import com.coddicted.buzzma.notification.entity.NotificationPayload;
import com.coddicted.buzzma.notification.entity.NotificationStatus;
import com.coddicted.buzzma.notification.persistence.NotificationRepository;
import com.coddicted.buzzma.notification.publisher.EventPublisher;
import com.coddicted.buzzma.notification.service.NotificationService;
import com.coddicted.buzzma.shared.common.BaseCrudService;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class NotificationServiceImpl extends BaseCrudService implements NotificationService {

  private final NotificationRepository notificationRepository;
  private final EventPublisher eventPublisher;

  public NotificationServiceImpl(
      final NotificationRepository notificationRepository, final EventPublisher eventPublisher) {
    this.notificationRepository = notificationRepository;
    this.eventPublisher = eventPublisher;
  }

  @Override
  public List<Notification> create(
      final String title, final String message, final List<UUID> userIds, final UUID requesterId) {
    final List<Notification> notifications =
        userIds.stream()
            .map(userId -> buildNotification(title, message, userId, requesterId))
            .toList();
    final List<Notification> saved = this.notificationRepository.saveAll(notifications);
    userIds.forEach(this.eventPublisher::publishNotification);
    return saved;
  }

  @Override
  public Notification create(
      final String title, final String message, final UUID userId, final UUID requesterId) {
    final Notification saved =
        this.notificationRepository.save(buildNotification(title, message, userId, requesterId));
    this.eventPublisher.publishNotification(userId);
    return saved;
  }

  @Override
  public List<Notification> listByUserId(final UUID userId) {
    return this.notificationRepository.findAllByUserIdAndIsDeletedFalse(userId);
  }

  @Override
  public Page<Notification> listByUserId(
      final UUID userId, final NotificationStatus status, final int page, final int size) {
    final PageRequest pageRequest =
        PageRequest.of(
            page, size, Sort.by(Sort.Order.desc("isPinned"), Sort.Order.desc("createdAt")));
    return this.notificationRepository.findAllByUserIdAndStatusAndIsDeletedFalse(
        userId, status, pageRequest);
  }

  @Override
  public long countUnread(final UUID userId) {
    return this.notificationRepository.countByUserIdAndStatusAndIsDeletedFalse(
        userId, NotificationStatus.NOTIFICATION_STATUS_UNREAD);
  }

  @Override
  public void markAsRead(final UUID notificationId, final UUID requesterId) {
    final Notification notification =
        mustFind(this.notificationRepository, notificationId, "Notification");
    notification.setStatus(NotificationStatus.NOTIFICATION_STATUS_READ);
    this.notificationRepository.save(notification);
  }

  @Override
  public void markAsUnread(final UUID notificationId, final UUID requesterId) {
    final Notification notification =
        mustFind(this.notificationRepository, notificationId, "Notification");
    notification.setStatus(NotificationStatus.NOTIFICATION_STATUS_UNREAD);
    this.notificationRepository.save(notification);
  }

  @Override
  public void pinNotification(final UUID notificationId, final UUID requesterId) {
    final Notification notification =
        mustFind(this.notificationRepository, notificationId, "Notification");
    notification.setPinned(true);
    this.notificationRepository.save(notification);
  }

  @Override
  public void markAllRead(final UUID requesterId) {
    final List<Notification> notifications =
        this.notificationRepository.findAllByUserIdAndIsDeletedFalse(requesterId);
    notifications.forEach(n -> n.setStatus(NotificationStatus.NOTIFICATION_STATUS_READ));
    this.notificationRepository.saveAll(notifications);
  }

  @Override
  public void markAllUnread(final UUID requesterId) {
    final List<Notification> notifications =
        this.notificationRepository.findAllByUserIdAndIsDeletedFalse(requesterId);
    notifications.forEach(n -> n.setStatus(NotificationStatus.NOTIFICATION_STATUS_UNREAD));
    this.notificationRepository.saveAll(notifications);
  }

  private Notification buildNotification(
      final String title, final String message, final UUID userId, final UUID requesterId) {
    return Notification.builder()
        .userId(userId)
        .createdBy(requesterId)
        .status(NotificationStatus.NOTIFICATION_STATUS_UNREAD)
        .isPinned(false)
        .payload(NotificationPayload.builder().title(title).message(message).build())
        .build();
  }
}
