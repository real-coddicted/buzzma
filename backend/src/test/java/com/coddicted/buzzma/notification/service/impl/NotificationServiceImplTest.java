package com.coddicted.buzzma.notification.service.impl;

import static com.coddicted.buzzma.notification.service.impl.Fixtures.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import com.coddicted.buzzma.notification.entity.Notification;
import com.coddicted.buzzma.notification.entity.NotificationStatus;
import com.coddicted.buzzma.notification.persistence.NotificationRepository;
import com.coddicted.buzzma.notification.publisher.EventPublisher;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

  @Mock private NotificationRepository mockNotificationRepository;
  @Mock private EventPublisher mockEventPublisher;
  private NotificationServiceImpl notificationService;

  @BeforeEach
  void setUp() {
    this.notificationService =
        new NotificationServiceImpl(this.mockNotificationRepository, this.mockEventPublisher);
  }

  @Test
  void testListByUserIdPagedSortsPinnedFirstThenNewest() {
    final Page<Notification> notificationPage = new PageImpl<>(List.of(NOTIFICATION_1));
    final PageRequest expectedPageRequest =
        PageRequest.of(0, 10, Sort.by(Sort.Order.desc("isPinned"), Sort.Order.desc("createdAt")));
    when(this.mockNotificationRepository.findAllByUserIdAndStatusAndIsDeletedFalse(
            USER_ID, NotificationStatus.NOTIFICATION_STATUS_UNREAD, expectedPageRequest))
        .thenReturn(notificationPage);

    final Page<Notification> result =
        this.notificationService.listByUserId(
            USER_ID, NotificationStatus.NOTIFICATION_STATUS_UNREAD, 0, 10);

    assertEquals(notificationPage, result);
  }

  @Test
  void testCountUnread() {
    when(this.mockNotificationRepository.countByUserIdAndStatusAndIsDeletedFalse(
            USER_ID, NotificationStatus.NOTIFICATION_STATUS_UNREAD))
        .thenReturn(5L);

    final long result = this.notificationService.countUnread(USER_ID);

    assertEquals(5L, result);
  }
}
