package com.coddicted.buzzma.notification.dto;

import java.util.List;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class PagedNotificationsResponseDto {
  List<NotificationResponseDto> items;
  long total;
  int page;
  int totalPages;
}
