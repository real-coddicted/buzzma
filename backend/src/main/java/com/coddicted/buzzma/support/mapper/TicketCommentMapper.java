package com.coddicted.buzzma.support.mapper;

import com.coddicted.buzzma.support.dto.TicketCommentRequestDto;
import com.coddicted.buzzma.support.dto.TicketCommentResponseDto;
import com.coddicted.buzzma.support.entity.TicketComment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface TicketCommentMapper {

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "ticketId", ignore = true)
  @Mapping(target = "authorId", ignore = true)
  @Mapping(target = "isDeleted", ignore = true)
  @Mapping(target = "createdBy", ignore = true)
  @Mapping(target = "updatedBy", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  TicketComment toEntity(TicketCommentRequestDto request);

  TicketCommentResponseDto toResponse(TicketComment comment);
}
