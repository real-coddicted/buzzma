package com.coddicted.buzzma.notification.persistence;

import com.coddicted.buzzma.notification.entity.Notification;
import com.coddicted.buzzma.notification.entity.NotificationStatus;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {
  List<Notification> findAllByUserIdAndIsDeletedFalse(UUID userId);

  Page<Notification> findAllByUserIdAndStatusAndIsDeletedFalse(
      UUID userId, NotificationStatus status, Pageable pageable);

  long countByUserIdAndStatusAndIsDeletedFalse(UUID userId, NotificationStatus status);
}
