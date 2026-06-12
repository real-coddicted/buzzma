package com.coddicted.buzzma.identity.mapper;

import com.coddicted.buzzma.identity.dto.UserSummaryDto;
import com.coddicted.buzzma.identity.entity.BuzzmaUser;
import java.util.UUID;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface UserMapper {

  @Mapping(source = "createdBy", target = "createdBy", qualifiedByName = "uuidToString")
  @Mapping(source = "updatedBy", target = "updatedBy", qualifiedByName = "uuidToString")
  @Mapping(target = "avatar", ignore = true)
  UserSummaryDto toUserSummaryDto(BuzzmaUser user);

  @Named("uuidToString")
  default String uuidToString(final UUID uuid) {
    if (uuid == null) {
      return null;
    }
    return uuid.toString();
  }
}
