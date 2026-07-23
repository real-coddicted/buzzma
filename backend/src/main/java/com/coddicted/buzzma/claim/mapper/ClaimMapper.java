package com.coddicted.buzzma.claim.mapper;

import com.coddicted.buzzma.campaign.entity.Deal;
import com.coddicted.buzzma.campaign.mapper.DealMapper;
import com.coddicted.buzzma.claim.dto.ClaimRequestDto;
import com.coddicted.buzzma.claim.dto.ClaimResponseDto;
import com.coddicted.buzzma.claim.dto.ClaimScreenshotResponseDto;
import com.coddicted.buzzma.claim.entity.Claim;
import com.coddicted.buzzma.claim.entity.ClaimScreenshot;
import java.util.List;
import java.util.UUID;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
    componentModel = "spring",
    uses = DealMapper.class,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ClaimMapper {

  @Mapping(source = "claim.id", target = "id")
  @Mapping(source = "claim.code", target = "code")
  @Mapping(source = "deal", target = "deal")
  @Mapping(source = "claim.status", target = "status")
  @Mapping(source = "currentStep", target = "currentStep")
  @Mapping(source = "claim.ecommerceOrderId", target = "ecommerceOrderId")
  @Mapping(source = "claim.amountPaise", target = "amountPaise")
  @Mapping(source = "claim.productName", target = "productName")
  @Mapping(source = "claim.sellerName", target = "sellerName")
  @Mapping(source = "claim.orderDate", target = "orderDate")
  @Mapping(source = "claim.accountName", target = "accountName")
  // TODO to be changed later when the screenshot data extraction
  //  is enhanced to fetch profile vs buyer name separately
  @Mapping(source = "claim.accountName", target = "orderedBy")
  @Mapping(source = "claim.reviewUrl", target = "reviewUrl")
  @Mapping(source = "screenshots", target = "screenshots")
  @Mapping(source = "claim.mediatorVerified", target = "mediatorVerified")
  @Mapping(source = "claim.score", target = "score")
  @Mapping(source = "claim.platform", target = "platform")
  @Mapping(source = "claim.reviewerComments", target = "reviewerComments")
  @Mapping(source = "claim.reviewerId", target = "reviewerId")
  @Mapping(source = "claim.reviewStatus", target = "reviewStatus")
  @Mapping(source = "claim.createdAt", target = "createdAt")
  @Mapping(source = "claim.updatedAt", target = "updatedAt")
  ClaimResponseDto toResponse(
      Claim claim, Deal deal, List<ClaimScreenshot> screenshots, int currentStep);

  ClaimScreenshotResponseDto toScreenshotResponse(ClaimScreenshot screenshot);

  List<ClaimScreenshotResponseDto> toScreenshotResponse(List<ClaimScreenshot> screenshots);

  @Mapping(source = "request.campaignId", target = "campaignId")
  @Mapping(source = "request.dealId", target = "dealId")
  @Mapping(source = "request.platform", target = "platform")
  @Mapping(source = "request.orderId", target = "ecommerceOrderId")
  @Mapping(source = "request.amount", target = "amountPaise")
  @Mapping(source = "request.productName", target = "productName")
  @Mapping(source = "request.sellerName", target = "sellerName")
  @Mapping(source = "request.orderDate", target = "orderDate")
  @Mapping(source = "request.accountName", target = "accountName")
  @Mapping(source = "ownerId", target = "ownerId")
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "code", ignore = true)
  @Mapping(target = "status", ignore = true)
  @Mapping(target = "currentStep", ignore = true)
  @Mapping(target = "reviewStatus", ignore = true)
  @Mapping(target = "reviewerId", ignore = true)
  @Mapping(target = "mediatorVerified", ignore = true)
  @Mapping(target = "score", ignore = true)
  @Mapping(target = "reviewerComments", ignore = true)
  @Mapping(target = "reviewUrl", ignore = true)
  @Mapping(target = "isDeleted", ignore = true)
  @Mapping(target = "createdBy", ignore = true)
  @Mapping(target = "updatedBy", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  Claim toEntity(ClaimRequestDto request, UUID ownerId);
}
