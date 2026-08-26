package com.coddicted.buzzma.claim.mapper;

import com.coddicted.buzzma.claim.dto.ClaimReviewWorksheetResponseDto;
import com.coddicted.buzzma.claim.dto.ClaimReviewWorksheetRowResponseDto;
import com.coddicted.buzzma.claim.entity.ClaimReviewWorksheet;
import com.coddicted.buzzma.claim.entity.ClaimReviewWorksheetRow;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ClaimReviewWorksheetMapper {

  @Mapping(target = "rowsProcessed", constant = "0")
  ClaimReviewWorksheetResponseDto toResponse(ClaimReviewWorksheet worksheet);

  ClaimReviewWorksheetRowResponseDto toRowResponse(ClaimReviewWorksheetRow row);
}
