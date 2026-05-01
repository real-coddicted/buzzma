package com.coddicted.buzzma.identity.mapper;

import com.coddicted.buzzma.identity.api.UsersRequestDto;
import com.coddicted.buzzma.identity.api.UsersResponseDto;
import com.coddicted.buzzma.identity.entity.BuzzmaUser;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface UsersMapper {

  BuzzmaUser toEntity(UsersRequestDto request);

  UsersResponseDto toResponse(BuzzmaUser entity);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  void update(UsersRequestDto request, @MappingTarget BuzzmaUser entity);
}
