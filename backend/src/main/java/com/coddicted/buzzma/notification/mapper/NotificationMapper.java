package com.coddicted.buzzma.notification.mapper;

import com.coddicted.buzzma.notification.dto.NotificationResponseDto;
import com.coddicted.buzzma.notification.entity.Notification;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface NotificationMapper {

  @Mapping(source = "payload.title", target = "title")
  @Mapping(source = "payload.message", target = "message")
  @Mapping(source = "pinned", target = "isPinned")
  NotificationResponseDto toResponse(Notification notification);

  List<NotificationResponseDto> toResponses(List<Notification> notifications);
}
