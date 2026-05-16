package com.coddicted.buzzma.order.mapper;

import com.coddicted.buzzma.campaign.entity.Campaign;
import com.coddicted.buzzma.campaign.mapper.DealMapper;
import com.coddicted.buzzma.order.dto.OrderRequestDto;
import com.coddicted.buzzma.order.dto.OrderResponseDto;
import com.coddicted.buzzma.order.entity.Order;
import com.coddicted.buzzma.shared.enums.OrderWorkflowStatus;
import java.util.UUID;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
    componentModel = "spring",
    uses = DealMapper.class,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface OrderMapper {

  @Mapping(source = "order.id", target = "id")
  @Mapping(source = "order.campaignId", target = "campaignId")
  @Mapping(source = "campaign", target = "deal")
  @Mapping(source = "order.status", target = "status")
  @Mapping(expression = "java(currentStep(order.getStatus()))", target = "currentStep")
  @Mapping(source = "order.ecommerceOrderId", target = "ecommerceOrderId")
  @Mapping(source = "order.amountPaise", target = "amountPaise")
  @Mapping(source = "order.productName", target = "productName")
  @Mapping(source = "order.sellerName", target = "sellerName")
  @Mapping(source = "order.orderDate", target = "orderDate")
  @Mapping(source = "order.accountName", target = "accountName")
  @Mapping(source = "order.reviewUrl", target = "reviewUrl")
  @Mapping(source = "order.screenshots", target = "screenshots")
  @Mapping(source = "order.overallVerified", target = "overallVerified")
  @Mapping(source = "order.overallScore", target = "overallScore")
  @Mapping(source = "order.rejectionNote", target = "rejectionNote")
  @Mapping(source = "order.comments", target = "comments")
  @Mapping(source = "order.createdAt", target = "createdAt")
  @Mapping(source = "order.updatedAt", target = "updatedAt")
  OrderResponseDto toResponse(Order order, Campaign campaign);

  @Mapping(source = "request.campaignId", target = "campaignId")
  @Mapping(source = "request.orderId", target = "ecommerceOrderId")
  @Mapping(source = "request.amount", target = "amountPaise")
  @Mapping(source = "request.productName", target = "productName")
  @Mapping(source = "request.sellerName", target = "sellerName")
  @Mapping(source = "request.orderDate", target = "orderDate")
  @Mapping(source = "request.accountName", target = "accountName")
  @Mapping(source = "buyerId", target = "buyerId")
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "status", ignore = true)
  @Mapping(target = "orderScreenshotKey", ignore = true)
  @Mapping(target = "reviewUrl", ignore = true)
  @Mapping(target = "reviewScreenshotKey", ignore = true)
  @Mapping(target = "returnScreenshotKey", ignore = true)
  @Mapping(target = "rejectionNote", ignore = true)
  @Mapping(target = "isDeleted", ignore = true)
  @Mapping(target = "createdBy", ignore = true)
  @Mapping(target = "updatedBy", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  Order toEntity(OrderRequestDto request, UUID buyerId);

  default int currentStep(final OrderWorkflowStatus status) {
    return switch (status) {
      case ORDERED -> 1;
      case PROOF_SUBMITTED -> 2;
      default -> 3;
    };
  }
}
