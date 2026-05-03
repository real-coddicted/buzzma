package com.coddicted.buzzma.identity.mapper;

import com.coddicted.buzzma.identity.dto.UserRequestDto;
import com.coddicted.buzzma.identity.dto.UserResponseDto;
import com.coddicted.buzzma.identity.entity.BuzzmaUser;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface UsersMapper {

  BuzzmaUser toEntity(UserRequestDto request);

  UserResponseDto toResponse(BuzzmaUser entity);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  void update(UserRequestDto request, @MappingTarget BuzzmaUser entity);
}
