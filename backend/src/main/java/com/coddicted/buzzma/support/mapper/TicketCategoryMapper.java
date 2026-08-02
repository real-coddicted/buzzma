package com.coddicted.buzzma.support.mapper;

import com.coddicted.buzzma.support.dto.TicketCategoryResponseDto;
import com.coddicted.buzzma.support.dto.TicketSubCategoryResponseDto;
import com.coddicted.buzzma.support.entity.TicketCategory;
import com.coddicted.buzzma.support.entity.TicketCategoryCode;
import com.coddicted.buzzma.support.entity.TicketSubCategory;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
    componentModel = "spring",
    imports = TicketCategoryCode.class,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface TicketCategoryMapper {

  @Mapping(target = "subCategories", ignore = true)
  @Mapping(target = "requiresOrderId", ignore = true)
  @Mapping(target = "requiresDealId", ignore = true)
  @Mapping(
      target = "requiresClaimCode",
      expression = "java(TicketCategoryCode.CLAIM.equals(category.getCode()))")
  @Mapping(
      target = "requiresCampaignCode",
      expression = "java(TicketCategoryCode.CAMPAIGN.equals(category.getCode()))")
  TicketCategoryResponseDto toResponse(TicketCategory category);

  default TicketCategoryResponseDto toResponse(
      final TicketCategory category, final List<TicketSubCategory> subCategories) {
    return toResponse(category).toBuilder()
        .subCategories(toSubCategoryResponseList(subCategories))
        .build();
  }

  TicketSubCategoryResponseDto toSubCategoryResponse(TicketSubCategory subCategory);

  List<TicketSubCategoryResponseDto> toSubCategoryResponseList(
      List<TicketSubCategory> subCategories);
}
