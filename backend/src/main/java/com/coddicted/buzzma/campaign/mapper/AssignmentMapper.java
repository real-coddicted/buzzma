package com.coddicted.buzzma.campaign.mapper;

import com.coddicted.buzzma.campaign.dto.AssignmentResponseDto;
import com.coddicted.buzzma.campaign.model.Assignment;
import java.net.URL;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface AssignmentMapper {

  @Mapping(source = "campaignAssignment.id", target = "id")
  @Mapping(source = "campaignAssignment.assigneeId", target = "ownerId")
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
  @Mapping(source = "campaignAssignment.campaignPricePaise", target = "offeredPricePaise")
  @Mapping(source = "campaign.returnWindowDays", target = "returnWindowDays")
  @Mapping(source = "campaign.termsAndConditions", target = "termsAndConditions")
  @Mapping(source = "campaign.sellerName", target = "sellerName")
  AssignmentResponseDto toResponse(Assignment assignment);

  List<AssignmentResponseDto> toResponse(List<Assignment> assignments);

  @Named("urlToString")
  default String urlToString(final URL url) {
    return url != null ? url.toString() : null;
  }
}
