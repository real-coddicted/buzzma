package com.coddicted.buzzma.connection.mapper;

import com.coddicted.buzzma.connection.dto.ConnectionRequestDto;
import com.coddicted.buzzma.connection.dto.ConnectionResponseDto;
import com.coddicted.buzzma.connection.entity.Connection;
import java.util.Set;
import java.util.UUID;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ConnectionMapper {

  @Mapping(source = "request.toUserId", target = "toUserId")
  @Mapping(source = "fromUserId", target = "fromUserId")
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "status", ignore = true)
  @Mapping(target = "isDeleted", ignore = true)
  @Mapping(target = "createdBy", ignore = true)
  @Mapping(target = "updatedBy", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  Connection toEntity(ConnectionRequestDto request, UUID fromUserId);

  ConnectionResponseDto toResponse(Connection connection);

  Set<ConnectionResponseDto> toResponses(Set<Connection> connections);
}
