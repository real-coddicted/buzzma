package com.coddicted.buzzma.mediator.mapper;

import com.coddicted.buzzma.mediator.api.PendingConnectionsRequestDto;
import com.coddicted.buzzma.mediator.api.PendingConnectionsResponseDto;
import com.coddicted.buzzma.mediator.persistence.PendingConnectionsEntity;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface PendingConnectionsMapper {

  PendingConnectionsEntity toEntity(PendingConnectionsRequestDto request);

  PendingConnectionsResponseDto toResponse(PendingConnectionsEntity entity);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  void update(PendingConnectionsRequestDto request, @MappingTarget PendingConnectionsEntity entity);
}
