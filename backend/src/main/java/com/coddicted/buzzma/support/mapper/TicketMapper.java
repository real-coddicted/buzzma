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
  @Mapping(target = "code", ignore = true)
  @Mapping(target = "assigneeId", ignore = true)
  @Mapping(target = "isDeleted", ignore = true)
  @Mapping(target = "createdBy", ignore = true)
  @Mapping(target = "updatedBy", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  Ticket toEntity(TicketRequestDto request);

  @Mapping(target = "dealId", ignore = true)
  @Mapping(target = "closedAt", ignore = true)
  @Mapping(target = "raisedByName", ignore = true)
  @Mapping(target = "assigneeName", ignore = true)
  @Mapping(target = "categoryName", ignore = true)
  @Mapping(target = "subCategoryName", ignore = true)
  TicketResponseDto toResponse(Ticket ticket);

  @Mapping(target = "dealId", ignore = true)
  @Mapping(target = "closedAt", ignore = true)
  TicketResponseDto toResponse(
      Ticket ticket,
      String raisedByName,
      String assigneeName,
      String categoryName,
      String subCategoryName);
}
