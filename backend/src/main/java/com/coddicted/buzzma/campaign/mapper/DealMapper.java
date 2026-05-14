package com.coddicted.buzzma.campaign.mapper;

import com.coddicted.buzzma.campaign.dto.DealResponseDto;
import com.coddicted.buzzma.campaign.entity.Campaign;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface DealMapper {

  @Mapping(source = "product.name", target = "productName")
  @Mapping(source = "product.imageUrl", target = "productImageUrl")
  @Mapping(source = "product.productLink", target = "productUrl")
  @Mapping(source = "product.pricePaise", target = "originalPricePaise")
  @Mapping(source = "campaignPricePaise", target = "offeredPricePaise")
  @Mapping(source = "type", target = "dealType")
  DealResponseDto toResponse(Campaign campaign);
}
