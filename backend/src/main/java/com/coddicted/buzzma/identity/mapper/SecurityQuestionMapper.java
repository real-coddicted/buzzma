package com.coddicted.buzzma.identity.mapper;

import com.coddicted.buzzma.identity.api.SecurityQuestionRequestDto;
import com.coddicted.buzzma.identity.api.SecurityQuestionResponseDto;
import com.coddicted.buzzma.identity.entity.SecurityQuestion;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface SecurityQuestionMapper {

  SecurityQuestion toEntity(SecurityQuestionRequestDto request);

  SecurityQuestionResponseDto toResponse(SecurityQuestion entity);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  void update(SecurityQuestionRequestDto request, @MappingTarget SecurityQuestion entity);
}
