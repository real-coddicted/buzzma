package com.coddicted.buzzma.campaign.mapper;

import com.coddicted.buzzma.campaign.dto.AssignmentResponseDto;
import com.coddicted.buzzma.campaign.dto.AssignmentSummaryResponseDto;
import com.coddicted.buzzma.campaign.dto.AssignmentSummaryView;
import com.coddicted.buzzma.campaign.model.Assignment;
import java.math.BigInteger;
import java.net.URL;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface AssignmentMapper {

  @Mapping(source = "campaignAssignment.id", target = "id")
  @Mapping(source = "campaignAssignment.campaignId", target = "campaignId")
  @Mapping(source = "campaignAssignment.assigneeId", target = "ownerId")
  @Mapping(source = "campaign.product.name", target = "productName")
  @Mapping(source = "campaign.product.brandName", target = "productBrandName")
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
  @Mapping(source = "campaign.status", target = "campaignStatus")
  @Mapping(source = "campaign.product.pricePaise", target = "originalPricePaise")
  @Mapping(source = "campaignAssignment.adjustedCampaignPricePaise", target = "offeredPricePaise")
  @Mapping(source = "campaignAssignment.commissionOfferedPaise", target = "commissionOfferedPaise")
  @Mapping(source = "campaignAssignment.slotLimit", target = "slotLimit")
  @Mapping(source = "campaign.returnWindowDays", target = "returnWindowDays")
  @Mapping(source = "campaign.termsAndConditions", target = "termsAndConditions")
  @Mapping(source = "campaign.sellerName", target = "sellerName")
  @Mapping(source = "campaign.affiliateLinkAllowed", target = "affiliateLinkAllowed")
  AssignmentResponseDto toResponse(Assignment assignment);

  default AssignmentSummaryResponseDto toSummaryResponse(final AssignmentSummaryView view) {
    return AssignmentSummaryResponseDto.builder()
        .id(view.id())
        .productName(view.productName())
        .productImageUrl(view.productImageUrl() != null ? view.productImageUrl().toString() : null)
        .platform(view.platform())
        .dealType(view.dealType())
        .campaignStatus(view.campaignStatus())
        .originalPricePaise(view.originalPricePaise())
        .offeredPricePaise(
            view.offeredPricePaise() != null ? view.offeredPricePaise() : BigInteger.ZERO)
        .slotLimit(view.slotLimit())
        .build();
  }

  @Named("urlToString")
  default String urlToString(final URL url) {
    return url != null ? url.toString() : null;
  }
}
