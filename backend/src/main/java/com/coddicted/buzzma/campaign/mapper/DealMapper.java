package com.coddicted.buzzma.campaign.mapper;

import com.coddicted.buzzma.campaign.api.DealRequestDto;
import com.coddicted.buzzma.campaign.api.DealResponseDto;
import com.coddicted.buzzma.campaign.entity.Deal;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface DealMapper {

  Deal toEntity(DealRequestDto request);

  DealResponseDto toResponse(Deal entity);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  void update(DealRequestDto request, @MappingTarget Deal entity);
}
