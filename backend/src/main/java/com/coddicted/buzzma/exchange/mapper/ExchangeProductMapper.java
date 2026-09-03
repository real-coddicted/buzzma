package com.coddicted.buzzma.exchange.mapper;

import com.coddicted.buzzma.exchange.dto.ExchangeProductRequestDto;
import com.coddicted.buzzma.exchange.dto.ExchangeProductResponseDto;
import com.coddicted.buzzma.exchange.entity.AgencyExchangeProduct;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ExchangeProductMapper {

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "agencyId", ignore = true)
  @Mapping(target = "isDeleted", ignore = true)
  @Mapping(target = "createdBy", ignore = true)
  @Mapping(target = "updatedBy", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  AgencyExchangeProduct toEntity(ExchangeProductRequestDto request);

  ExchangeProductResponseDto toResponse(AgencyExchangeProduct product);
}
