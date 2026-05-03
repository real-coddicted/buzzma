package com.coddicted.buzzma.identity.mapper;

import com.coddicted.buzzma.identity.api.InviteRequestDto;
import com.coddicted.buzzma.identity.api.InviteResponseDto;
import com.coddicted.buzzma.identity.entity.Invite;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface InvitesMapper {

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "code", ignore = true)
  @Mapping(target = "ownerId", ignore = true)
  @Mapping(target = "status", ignore = true)
  @Mapping(target = "createdBy", ignore = true)
  @Mapping(target = "updatedBy", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  @Mapping(target = "isDeleted", ignore = true)
  Invite toEntity(InviteRequestDto request);

  InviteResponseDto toResponse(Invite entity);
}
