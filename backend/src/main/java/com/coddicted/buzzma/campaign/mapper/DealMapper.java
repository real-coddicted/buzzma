package com.coddicted.buzzma.campaign.mapper;

import com.coddicted.buzzma.campaign.dto.AssignmentResponseDto;
import com.coddicted.buzzma.campaign.dto.DealResponseDto;
import com.coddicted.buzzma.campaign.entity.Campaign;
import com.coddicted.buzzma.campaign.entity.Deal;
import java.net.URL;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface DealMapper {

  @Mapping(source = "product.name", target = "productName")
  @Mapping(source = "product.imageUrl", target = "productImageUrl", qualifiedByName = "urlToString")
  @Mapping(source = "product.productLink", target = "productUrl", qualifiedByName = "urlToString")
  @Mapping(source = "product.pricePaise", target = "originalPricePaise")
  @Mapping(source = "campaignPricePaise", target = "offeredPricePaise")
  @Mapping(source = "type", target = "dealType")
  AssignmentResponseDto toResponse(Campaign campaign);

  @Mapping(source = "campaign.product.name", target = "productName")
  @Mapping(
      source = "campaign.product.imageUrl",
      target = "productImageUrl",
      qualifiedByName = "urlToString")
  @Mapping(
      source = "campaign.product.productLink",
      target = "productUrl",
      qualifiedByName = "urlToString")
  @Mapping(source = "campaign.platform", target = "platform")
  @Mapping(source = "campaign.type", target = "dealType")
  @Mapping(source = "campaign.product.pricePaise", target = "originalPricePaise")
  @Mapping(source = "dealPricePaise", target = "offeredPricePaise")
  @Mapping(source = "campaign.returnWindowDays", target = "returnWindowDays")
  @Mapping(source = "campaign.termsAndConditions", target = "termsAndConditions")
  @Mapping(source = "campaign.sellerName", target = "sellerName")
  DealResponseDto toDealResponse(Deal deal);

  List<DealResponseDto> toDealResponse(List<Deal> deals);

  @Named("urlToString")
  default String urlToString(final URL url) {
    return url != null ? url.toString() : null;
  }
}
