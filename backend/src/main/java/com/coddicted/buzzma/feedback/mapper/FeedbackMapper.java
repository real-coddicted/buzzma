package com.coddicted.buzzma.feedback.mapper;

import com.coddicted.buzzma.feedback.dto.FeedbackRequestDto;
import com.coddicted.buzzma.feedback.dto.FeedbackResponseDto;
import com.coddicted.buzzma.feedback.entity.Feedback;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface FeedbackMapper {

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "userId", ignore = true)
  @Mapping(target = "isDeleted", ignore = true)
  @Mapping(target = "createdBy", ignore = true)
  @Mapping(target = "updatedBy", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  Feedback toEntity(FeedbackRequestDto request);

  FeedbackResponseDto toResponse(Feedback feedback);
}
