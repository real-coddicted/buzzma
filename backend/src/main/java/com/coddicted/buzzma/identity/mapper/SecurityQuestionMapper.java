package com.coddicted.buzzma.identity.mapper;

import com.coddicted.buzzma.identity.dto.SecurityQuestionRequestDto;
import com.coddicted.buzzma.identity.dto.SecurityQuestionResponseDto;
import com.coddicted.buzzma.identity.entity.SecurityQuestion;
import com.coddicted.buzzma.identity.entity.SecurityQuestionWrapper;
import java.util.List;
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

  SecurityQuestionResponseDto toResponse(SecurityQuestionWrapper wrapper);

  List<SecurityQuestionResponseDto> toResponseList(List<SecurityQuestionWrapper> wrappers);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  void update(SecurityQuestionRequestDto request, @MappingTarget SecurityQuestion entity);
}
