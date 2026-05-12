package com.coddicted.buzzma.extraction.mapper;

import com.coddicted.buzzma.extraction.dto.ExtractionJobResponseDto;
import com.coddicted.buzzma.extraction.entity.ExtractionJob;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ExtractionMapper {

  ExtractionJobResponseDto toResponse(ExtractionJob job);
}
