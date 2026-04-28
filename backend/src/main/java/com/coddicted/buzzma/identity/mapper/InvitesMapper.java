package com.coddicted.buzzma.identity.mapper;

import com.coddicted.buzzma.identity.api.InvitesRequestDto;
import com.coddicted.buzzma.identity.api.InvitesResponseDto;
import com.coddicted.buzzma.identity.entity.InviteEntity;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface InvitesMapper {

  InviteEntity toEntity(InvitesRequestDto request);

  InvitesResponseDto toResponse(InviteEntity entity);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  void update(InvitesRequestDto request, @MappingTarget InviteEntity entity);
}
