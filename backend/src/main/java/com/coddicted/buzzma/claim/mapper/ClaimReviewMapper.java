package com.coddicted.buzzma.claim.mapper;

import com.coddicted.buzzma.claim.dto.ClaimReviewResponseDto;
import com.coddicted.buzzma.claim.model.ClaimReviewModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ClaimReviewMapper {

  @Mapping(source = "claim.id", target = "id")
  @Mapping(source = "campaign.id", target = "campaignId")
  @Mapping(source = "campaign.title", target = "campaignName")
  @Mapping(source = "claim.dealId", target = "dealId")
  @Mapping(source = "dealOwnerId", target = "dealOwnerId")
  @Mapping(source = "dealOwnerName", target = "dealOwnerName")
  @Mapping(source = "buyerName", target = "buyerName")
  @Mapping(source = "claim.id", target = "claimId")
  @Mapping(source = "claim.status", target = "claimStatus")
  @Mapping(source = "claim.ecommerceOrderId", target = "ecommerceOrderId")
  @Mapping(source = "claim.mediatorVerified", target = "mediatorVerified")
  @Mapping(source = "claim.score", target = "matchScore")
  @Mapping(source = "claim.reviewStatus", target = "claimReviewStatus")
  @Mapping(source = "campaign.platform", target = "platform")
  @Mapping(source = "claim.orderDate", target = "orderDate")
  @Mapping(source = "campaign.product.brandName", target = "brandName")
  @Mapping(source = "claim.createdAt", target = "createdAt")
  @Mapping(source = "claim.updatedAt", target = "updatedAt")
  ClaimReviewResponseDto toResponse(ClaimReviewModel model);
}
