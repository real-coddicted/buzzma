package com.coddicted.buzzma.support.mapper;

import com.coddicted.buzzma.support.api.TicketCommentsRequestDto;
import com.coddicted.buzzma.support.api.TicketCommentsResponseDto;
import com.coddicted.buzzma.support.persistence.TicketCommentsEntity;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface TicketCommentsMapper {

  TicketCommentsEntity toEntity(TicketCommentsRequestDto request);

  TicketCommentsResponseDto toResponse(TicketCommentsEntity entity);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  void update(TicketCommentsRequestDto request, @MappingTarget TicketCommentsEntity entity);
}
