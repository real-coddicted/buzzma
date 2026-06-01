package com.coddicted.buzzma.campaign.mapper;

import com.coddicted.buzzma.campaign.dto.CampaignAssignmentRequestDto;
import com.coddicted.buzzma.campaign.dto.CampaignRequestDto;
import com.coddicted.buzzma.campaign.dto.CampaignResponseDto;
import com.coddicted.buzzma.campaign.dto.CampaignSummaryResponseDto;
import com.coddicted.buzzma.campaign.entity.Campaign;
import com.coddicted.buzzma.campaign.entity.CampaignAssignment;
import com.coddicted.buzzma.campaign.model.CampaignSummary;
import java.util.List;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
    componentModel = "spring",
    uses = {ProductMapper.class, CampaignAssignmentMapper.class},
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
  @Mapping(source = "assignees", target = "assignmentsDraft")
  Campaign toCampaignEntity(CampaignRequestDto request);

  @Mapping(source = "product.id", target = "productId")
  @Mapping(source = "product.name", target = "productName")
  @Mapping(source = "product.brandName", target = "productBrandName")
  @Mapping(source = "product.imageUrl", target = "productImageUrl")
  @Mapping(source = "product.productLink", target = "productLink")
  @Mapping(source = "product.pricePaise", target = "productPricePaise")
  @Mapping(source = "type", target = "campaignType")
  @Mapping(target = "assignments", ignore = true)
  CampaignResponseDto toResponse(Campaign entity);

  @Mapping(source = "campaign.product.id", target = "productId")
  @Mapping(source = "campaign.product.name", target = "productName")
  @Mapping(source = "campaign.product.brandName", target = "productBrandName")
  @Mapping(source = "campaign.product.imageUrl", target = "productImageUrl")
  @Mapping(source = "campaign.product.productLink", target = "productLink")
  @Mapping(source = "campaign.product.pricePaise", target = "productPricePaise")
  @Mapping(source = "campaign.type", target = "campaignType")
  @Mapping(source = "assignments", target = "assignments")
  CampaignResponseDto toResponse(Campaign campaign, List<CampaignAssignment> assignments);

  @Mapping(source = "campaign.product.id", target = "productId")
  @Mapping(source = "campaign.product.name", target = "productName")
  @Mapping(source = "campaign.product.brandName", target = "productBrandName")
  @Mapping(source = "campaign.product.imageUrl", target = "productImageUrl")
  @Mapping(source = "campaign.product.productLink", target = "productLink")
  @Mapping(source = "campaign.product.pricePaise", target = "productPricePaise")
  @Mapping(source = "campaign.type", target = "campaignType")
  @Mapping(source = "draftAssignments", target = "assignments")
  CampaignResponseDto toResponseFromDraft(
      Campaign campaign, List<CampaignAssignmentRequestDto> draftAssignments);

  @Mapping(source = "campaign.id", target = "campaignId")
  @Mapping(source = "campaign.title", target = "title")
  @Mapping(source = "campaign.product.imageUrl", target = "productImageUrl")
  @Mapping(source = "campaign.product.name", target = "productName")
  @Mapping(source = "campaign.product.brandName", target = "productBrandName")
  @Mapping(source = "campaign.startDate", target = "startDate")
  @Mapping(source = "campaign.endDate", target = "endDate")
  @Mapping(source = "campaign.status", target = "status")
  @Mapping(source = "campaign.platform", target = "platform")
  @Mapping(source = "campaign.type", target = "type")
  @Mapping(source = "campaign.totalSlots", target = "totalSlots")
  @Mapping(source = "slotsClaimed", target = "slotsClaimed")
  @Mapping(
      target = "budgetPaise",
      expression =
          "java(summary.getCampaign().getCampaignPricePaise() != null"
              + " && summary.getCampaign().getTotalSlots() != null"
              + " ? summary.getCampaign().getCampaignPricePaise()"
              + ".multiply(java.math.BigInteger.valueOf(summary.getCampaign().getTotalSlots()))"
              + " : null)")
  CampaignSummaryResponseDto toSummary(CampaignSummary summary);

  List<CampaignSummaryResponseDto> toSummaries(List<CampaignSummary> summaries);

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
  @Mapping(source = "assignees", target = "assignmentsDraft")
  void updateCampaign(CampaignRequestDto request, @MappingTarget Campaign entity);
}
