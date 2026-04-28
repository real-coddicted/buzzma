package com.coddicted.buzzma.campaign.mapper;

import com.coddicted.buzzma.campaign.api.CampaignRequestDto;
import com.coddicted.buzzma.campaign.api.CampaignResponseDto;
import com.coddicted.buzzma.campaign.entity.Campaign;
import com.coddicted.buzzma.campaign.entity.CampaignAssignment;
import com.coddicted.buzzma.campaign.entity.OwnerType;
import com.coddicted.buzzma.campaign.entity.Platform;
import com.coddicted.buzzma.campaign.entity.Product;
import java.net.MalformedURLException;
import java.net.URL;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
    componentModel = "spring",
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
  @Mapping(target = "campaignPricePaise", ignore = true)
  @Mapping(target = "commissionOfferedPaise", ignore = true)
  @Mapping(target = "returnWindowDays", ignore = true)
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

  // ── Product ─────────────────────────────────────────────────────────────────

  @Mapping(target = "id", ignore = true)
  @Mapping(source = "productBrandName", target = "name")
  @Mapping(source = "productImageUrl", target = "imageUrl", qualifiedByName = "stringToUrl")
  @Mapping(source = "productUrl", target = "productLink", qualifiedByName = "stringToUrl")
  @Mapping(source = "originalPricePaise", target = "pricePaise")
  @Mapping(source = "platform", target = "platform", qualifiedByName = "stringToPlatform")
  Product toProductEntity(CampaignRequestDto request);

  // ── CampaignAssignment (initial template — campaignId/assignee set by service) ──

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "campaignId", ignore = true)
  @Mapping(target = "assignedToType", ignore = true)
  @Mapping(target = "assignedToCode", ignore = true)
  @Mapping(target = "commissionChargedPaise", ignore = true)
  @Mapping(target = "createdBy", ignore = true)
  @Mapping(target = "updatedBy", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  @Mapping(target = "isDeleted", ignore = true)
  @Mapping(source = "commissionOfferedPaise", target = "commissionOfferedPaise")
  @Mapping(source = "totalSlots", target = "slotLimit")
  CampaignAssignment toAssignmentEntity(CampaignRequestDto request);

  // ── Conversion helpers ───────────────────────────────────────────────────────

  @org.mapstruct.Named("stringToUrl")
  default URL stringToUrl(String value) {
    if (value == null || value.isBlank()) return null;
    try {
      return java.net.URI.create(value).toURL();
    } catch (MalformedURLException e) {
      throw new IllegalArgumentException("Invalid URL: " + value, e);
    }
  }

  @org.mapstruct.Named("stringToPlatform")
  default Platform stringToPlatform(String value) {
    if (value == null || value.isBlank()) return null;
    return Platform.valueOf(value.toUpperCase());
  }

  @org.mapstruct.Named("stringToOwnerType")
  default OwnerType stringToOwnerType(String value) {
    if (value == null || value.isBlank()) return null;
    return OwnerType.valueOf(value);
  }
}