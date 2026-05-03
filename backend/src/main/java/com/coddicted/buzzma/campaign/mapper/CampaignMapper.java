package com.coddicted.buzzma.campaign.mapper;

import com.coddicted.buzzma.campaign.dto.CampaignRequestDto;
import com.coddicted.buzzma.campaign.dto.CampaignResponseDto;
import com.coddicted.buzzma.campaign.entity.Campaign;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
    componentModel = "spring",
    uses = ProductMapper.class,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface CampaignMapper {

  // ── Campaign ────────────────────────────────────────────────────────────────

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "status", ignore = true)
  @Mapping(target = "createdBy", ignore = true)
  @Mapping(target = "updatedBy", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  @Mapping(target = "isDeleted", ignore = true)
  @Mapping(source = "campaignType", target = "type")
  @Mapping(source = "totalSlots", target = "totalSlots")
  Campaign toCampaignEntity(CampaignRequestDto request);

  @Mapping(source = "product.id", target = "productId")
  @Mapping(source = "product.name", target = "productName")
  @Mapping(source = "product.imageUrl", target = "productImageUrl")
  @Mapping(source = "product.productLink", target = "productLink")
  @Mapping(source = "product.pricePaise", target = "productPricePaise")
  @Mapping(source = "type", target = "campaignType")
  CampaignResponseDto toResponse(Campaign entity);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "product", ignore = true)
  @Mapping(target = "status", ignore = true)
  @Mapping(target = "createdBy", ignore = true)
  @Mapping(target = "updatedBy", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  @Mapping(target = "deleted", ignore = true)
  @Mapping(source = "campaignType", target = "type")
  void updateCampaign(CampaignRequestDto request, @MappingTarget Campaign entity);
}
