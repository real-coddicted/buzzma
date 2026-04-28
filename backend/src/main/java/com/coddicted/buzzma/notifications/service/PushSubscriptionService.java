package com.coddicted.buzzma.notifications.service;

import com.coddicted.buzzma.notifications.api.PushSubscriptionsRequestDto;
import com.coddicted.buzzma.notifications.api.PushSubscriptionsResponseDto;
import java.util.List;
import java.util.UUID;

public interface PushSubscriptionService {

  List<PushSubscriptionsResponseDto> list(int limit, int offset);

  PushSubscriptionsResponseDto getById(UUID id);

  PushSubscriptionsResponseDto create(PushSubscriptionsRequestDto request);

  PushSubscriptionsResponseDto update(UUID id, PushSubscriptionsRequestDto request);

  void delete(UUID id);
}
