package com.coddicted.buzzma.notifications.mapper;

import com.coddicted.buzzma.notifications.api.PushSubscriptionsRequestDto;
import com.coddicted.buzzma.notifications.api.PushSubscriptionsResponseDto;
import com.coddicted.buzzma.notifications.persistence.PushSubscriptionsEntity;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface PushSubscriptionsMapper {

  PushSubscriptionsEntity toEntity(PushSubscriptionsRequestDto request);

  PushSubscriptionsResponseDto toResponse(PushSubscriptionsEntity entity);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  void update(PushSubscriptionsRequestDto request, @MappingTarget PushSubscriptionsEntity entity);
}
