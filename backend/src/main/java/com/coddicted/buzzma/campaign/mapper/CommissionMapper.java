package com.coddicted.buzzma.campaign.mapper;

import com.coddicted.buzzma.campaign.dto.CommissionResponseDto;
import com.coddicted.buzzma.campaign.entity.Commission;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface CommissionMapper {

  CommissionResponseDto toResponse(Commission commission);
}
