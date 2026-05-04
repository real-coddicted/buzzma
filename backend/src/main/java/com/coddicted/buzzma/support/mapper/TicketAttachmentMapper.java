package com.coddicted.buzzma.support.mapper;

import com.coddicted.buzzma.support.dto.TicketAttachmentResponseDto;
import com.coddicted.buzzma.support.entity.TicketAttachment;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface TicketAttachmentMapper {

  TicketAttachmentResponseDto toResponse(TicketAttachment attachment);
}
