package com.coddicted.buzzma.campaign.mapper;

import com.coddicted.buzzma.campaign.api.CampaignRequestDto;
import com.coddicted.buzzma.campaign.api.CampaignResponseDto;
import com.coddicted.buzzma.campaign.entity.Campaign;
import org.mapstruct.*;

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
  @Mapping(source = "product.platform", target = "platform")
  @Mapping(source = "type", target = "campaignType")
  @Mapping(source = "ownerType", target = "ownerType", qualifiedByName = "stringToOwnerType")
  @Mapping(target = "allowedAgencies", ignore = true)
  @Mapping(target = "openToAll", ignore = true)
  @Mapping(target = "commissionOfferedPaise", ignore = true)
  CampaignResponseDto toResponse(Campaign entity);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "product", ignore = true)
  @Mapping(target = "status", ignore = true)
  @Mapping(target = "createdBy", ignore = true)
  @Mapping(target = "updatedBy", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  @Mapping(target = "isDeleted", ignore = true)
  @Mapping(source = "campaignType", target = "type")
  void updateCampaign(CampaignRequestDto request, @MappingTarget Campaign entity);

  @Named("stringToOwnerType")
  default OwnerType stringToOwnerType(String value) {
    if (value == null || value.isBlank()) return null;
    return OwnerType.valueOf(value);
  }
}