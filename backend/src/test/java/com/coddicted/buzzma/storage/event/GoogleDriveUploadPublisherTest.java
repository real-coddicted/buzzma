package com.coddicted.buzzma.storage.event;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.StringRedisTemplate;

@ExtendWith(MockitoExtension.class)
class GoogleDriveUploadPublisherTest {

  @Mock private StringRedisTemplate mockRedisTemplate;
  @Mock private ListOperations<String, String> mockListOps;
  @Spy private ObjectMapper objectMapper;
  @InjectMocks private GoogleDriveUploadPublisher publisher;

  @Test
  void enqueue_pushesJsonMessageToRedisQueue() throws Exception {
    final UUID screenshotId = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee");
    final UUID claimId = UUID.fromString("11111111-2222-3333-4444-555555555555");
    final GoogleDriveUploadMessage message =
        new GoogleDriveUploadMessage(
            screenshotId, claimId, "CAM001", "CLM001", "claims/abc.jpg", "SCREENSHOT_TYPE_ORDER");

    when(this.mockRedisTemplate.opsForList()).thenReturn(this.mockListOps);

    this.publisher.enqueue(message);

    final String expectedJson = this.objectMapper.writeValueAsString(message);
    verify(this.mockListOps).leftPush(eq(GoogleDriveUploadPublisher.QUEUE_KEY), eq(expectedJson));
  }
}
