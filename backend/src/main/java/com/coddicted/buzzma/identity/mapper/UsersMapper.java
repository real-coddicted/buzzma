package com.coddicted.buzzma.identity.mapper;

import com.coddicted.buzzma.identity.dto.UserRequestDto;
import com.coddicted.buzzma.identity.dto.UserResponseDto;
import com.coddicted.buzzma.identity.entity.BuzzmaUser;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface UsersMapper {

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "createdBy", ignore = true)
  @Mapping(target = "updatedBy", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  @Mapping(target = "isDeleted", ignore = true)
  BuzzmaUser toEntity(UserRequestDto request);

  @Mapping(target = "roles", ignore = true)
  @Mapping(target = "mediatorCode", ignore = true)
  @Mapping(target = "parentCode", ignore = true)
  @Mapping(target = "generatedCodes", ignore = true)
  @Mapping(target = "isVerifiedByMediator", ignore = true)
  @Mapping(target = "brandCode", ignore = true)
  @Mapping(target = "connectedAgencies", ignore = true)
  @Mapping(target = "kycStatus", ignore = true)
  @Mapping(target = "upiId", ignore = true)
  @Mapping(target = "qrCode", ignore = true)
  @Mapping(target = "bankAccountNumber", ignore = true)
  @Mapping(target = "bankIfsc", ignore = true)
  @Mapping(target = "bankName", ignore = true)
  @Mapping(target = "bankHolderName", ignore = true)
  @Mapping(target = "avatar", ignore = true)
  UserResponseDto toResponse(BuzzmaUser entity);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "createdBy", ignore = true)
  @Mapping(target = "updatedBy", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  @Mapping(target = "isDeleted", ignore = true)
  void update(UserRequestDto request, @MappingTarget BuzzmaUser entity);
}
