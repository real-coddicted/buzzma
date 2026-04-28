package com.coddicted.buzzma.identity.mapper;

import com.coddicted.buzzma.identity.api.SecurityQuestionsRequestDto;
import com.coddicted.buzzma.identity.api.SecurityQuestionsResponseDto;
import com.coddicted.buzzma.identity.entity.SecurityQuestionsEntity;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface SecurityQuestionsMapper {

  SecurityQuestionsEntity toEntity(SecurityQuestionsRequestDto request);

  SecurityQuestionsResponseDto toResponse(SecurityQuestionsEntity entity);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  void update(SecurityQuestionsRequestDto request, @MappingTarget SecurityQuestionsEntity entity);
}
