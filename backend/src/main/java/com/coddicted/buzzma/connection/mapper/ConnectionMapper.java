package com.coddicted.buzzma.connection.mapper;

import com.coddicted.buzzma.connection.dto.ConnectionResponseDto;
import com.coddicted.buzzma.connection.dto.ConnectionSummaryResponseDto;
import com.coddicted.buzzma.connection.entity.Connection;
import com.coddicted.buzzma.connection.model.ConnectionSummary;
import com.coddicted.buzzma.connection.model.ConnectionView;
import java.util.Set;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ConnectionMapper {

  @Mapping(target = "fromUserName", ignore = true)
  @Mapping(target = "fromUserRole", ignore = true)
  @Mapping(target = "toUserName", ignore = true)
  @Mapping(target = "toUserRole", ignore = true)
  ConnectionResponseDto toResponse(Connection connection);

  @Mapping(source = "connection.id", target = "id")
  @Mapping(source = "connection.fromUserId", target = "fromUserId")
  @Mapping(source = "connection.toUserId", target = "toUserId")
  @Mapping(source = "connection.status", target = "status")
  @Mapping(source = "connection.createdBy", target = "createdBy")
  @Mapping(source = "connection.updatedBy", target = "updatedBy")
  @Mapping(source = "connection.createdAt", target = "createdAt")
  @Mapping(source = "connection.updatedAt", target = "updatedAt")
  ConnectionResponseDto toResponse(ConnectionView view);

  Set<ConnectionResponseDto> toResponses(Set<ConnectionView> views);

  @Mapping(target = "total", expression = "java(summary.getConnected() + summary.getPending())")
  ConnectionSummaryResponseDto toResponse(ConnectionSummary summary);
}
