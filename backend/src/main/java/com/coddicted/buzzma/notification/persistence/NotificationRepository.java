package com.coddicted.buzzma.notification.persistence;

import com.coddicted.buzzma.notification.entity.Notification;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {
  List<Notification> findAllByUserIdAndIsDeletedFalse(UUID userId);
}
