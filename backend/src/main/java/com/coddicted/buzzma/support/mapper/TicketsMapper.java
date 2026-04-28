package com.coddicted.buzzma.support.mapper;

import com.coddicted.buzzma.support.api.TicketsRequestDto;
import com.coddicted.buzzma.support.api.TicketsResponseDto;
import com.coddicted.buzzma.support.persistence.TicketsEntity;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface TicketsMapper {

  @Mapping(target = "priority", ignore = true)
  TicketsEntity toEntity(TicketsRequestDto request);

  TicketsResponseDto toResponse(TicketsEntity entity);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  void update(TicketsRequestDto request, @MappingTarget TicketsEntity entity);
}
