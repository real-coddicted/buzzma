package com.coddicted.buzzma.identity.mapper;

import com.coddicted.buzzma.identity.api.SecurityQuestionRequestDto;
import com.coddicted.buzzma.identity.api.SecurityQuestionResponseDto;
import com.coddicted.buzzma.identity.entity.SecurityAnswer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface SecurityAnswerMapper {

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  @Mapping(target = "createdBy", ignore = true)
  @Mapping(target = "updatedBy", ignore = true)
  @Mapping(target = "isDeleted", ignore = true)
  @Mapping(source = "answer", target = "answerHash")
  SecurityAnswer toEntity(SecurityQuestionRequestDto request);

  SecurityQuestionResponseDto toResponse(SecurityAnswer entity);
}
