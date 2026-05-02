package com.coddicted.buzzma.identity.mapper;

import com.coddicted.buzzma.identity.api.SecurityQuestionsRequestDto;
import com.coddicted.buzzma.identity.api.SecurityQuestionsResponseDto;
import com.coddicted.buzzma.identity.entity.SecurityQuestion;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface SecurityQuestionMapper {

  SecurityQuestion toEntity(SecurityQuestionsRequestDto request);

  SecurityQuestionsResponseDto toResponse(SecurityQuestion entity);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  void update(SecurityQuestionsRequestDto request, @MappingTarget SecurityQuestion entity);
}
