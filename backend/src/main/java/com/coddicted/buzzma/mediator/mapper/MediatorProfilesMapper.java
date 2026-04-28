package com.coddicted.buzzma.mediator.mapper;

import com.coddicted.buzzma.mediator.api.MediatorProfilesRequestDto;
import com.coddicted.buzzma.mediator.api.MediatorProfilesResponseDto;
import com.coddicted.buzzma.mediator.persistence.MediatorProfilesEntity;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface MediatorProfilesMapper {

  MediatorProfilesEntity toEntity(MediatorProfilesRequestDto request);

  MediatorProfilesResponseDto toResponse(MediatorProfilesEntity entity);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  void update(MediatorProfilesRequestDto request, @MappingTarget MediatorProfilesEntity entity);
}
