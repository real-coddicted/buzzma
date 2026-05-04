package com.coddicted.buzzma.support.mapper;

import com.coddicted.buzzma.support.dto.TicketRequestDto;
import com.coddicted.buzzma.support.dto.TicketResponseDto;
import com.coddicted.buzzma.support.entity.Ticket;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface TicketMapper {

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "raisedBy", ignore = true)
  @Mapping(target = "status", ignore = true)
  @Mapping(target = "assigneeId", ignore = true)
  @Mapping(target = "isDeleted", ignore = true)
  @Mapping(target = "createdBy", ignore = true)
  @Mapping(target = "updatedBy", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  Ticket toEntity(TicketRequestDto request);

  TicketResponseDto toResponse(Ticket ticket);
}
