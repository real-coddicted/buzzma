package com.coddicted.buzzma.storage.event;

import static org.mockito.Mockito.verify;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.StringRedisTemplate;

@ExtendWith(MockitoExtension.class)
class GoogleDriveUploadPublisherTest {

  @Mock private StringRedisTemplate mockRedisTemplate;
  @Mock private ListOperations<String, String> mockListOps;
  @InjectMocks private GoogleDriveUploadPublisher publisher;

  @Test
  void enqueue_pushesScreenshotIdToRedisQueue() {
    final UUID screenshotId = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee");
    org.mockito.Mockito.when(this.mockRedisTemplate.opsForList()).thenReturn(this.mockListOps);

    this.publisher.enqueue(screenshotId);

    verify(this.mockListOps)
        .leftPush(GoogleDriveUploadPublisher.QUEUE_KEY, screenshotId.toString());
  }
}
