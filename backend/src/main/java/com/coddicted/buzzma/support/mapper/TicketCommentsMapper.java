package com.coddicted.buzzma.support.mapper;

import com.coddicted.buzzma.support.api.TicketCommentRequestDto;
import com.coddicted.buzzma.support.api.TicketCommentResponseDto;
import com.coddicted.buzzma.support.persistence.TicketCommentsEntity;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface TicketCommentsMapper {

  TicketCommentsEntity toEntity(TicketCommentRequestDto request);

  TicketCommentResponseDto toResponse(TicketCommentsEntity entity);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  void update(TicketCommentRequestDto request, @MappingTarget TicketCommentsEntity entity);
}
