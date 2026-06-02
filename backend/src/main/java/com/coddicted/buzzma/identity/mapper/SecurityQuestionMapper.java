package com.coddicted.buzzma.identity.mapper;

import com.coddicted.buzzma.identity.dto.SecurityQuestionRequestDto;
import com.coddicted.buzzma.identity.dto.SecurityQuestionResponseDto;
import com.coddicted.buzzma.identity.entity.SecurityQuestion;
import com.coddicted.buzzma.identity.entity.SecurityQuestionWrapper;
import java.util.List;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface SecurityQuestionMapper {

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "question", ignore = true)
  @Mapping(target = "createdBy", ignore = true)
  @Mapping(target = "updatedBy", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  @Mapping(target = "isDeleted", ignore = true)
  SecurityQuestion toEntity(SecurityQuestionRequestDto request);

  @Mapping(target = "userId", ignore = true)
  @Mapping(target = "questionId", ignore = true)
  SecurityQuestionResponseDto toResponse(SecurityQuestion entity);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  SecurityQuestionResponseDto toResponse(SecurityQuestionWrapper wrapper);

  List<SecurityQuestionResponseDto> toResponseList(List<SecurityQuestionWrapper> wrappers);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "question", ignore = true)
  @Mapping(target = "createdBy", ignore = true)
  @Mapping(target = "updatedBy", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  @Mapping(target = "isDeleted", ignore = true)
  void update(SecurityQuestionRequestDto request, @MappingTarget SecurityQuestion entity);
}
