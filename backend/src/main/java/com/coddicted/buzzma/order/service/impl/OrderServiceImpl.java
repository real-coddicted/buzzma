package com.coddicted.buzzma.order.service.impl;

import com.coddicted.buzzma.campaign.entity.Campaign;
import com.coddicted.buzzma.campaign.entity.CampaignStatus;
import com.coddicted.buzzma.campaign.persistence.CampaignRepository;
import com.coddicted.buzzma.order.entity.Order;
import com.coddicted.buzzma.order.persistence.OrderRepository;
import com.coddicted.buzzma.order.service.OrderService;
import com.coddicted.buzzma.shared.common.BaseCrudService;
import com.coddicted.buzzma.shared.enums.OrderWorkflowStatus;
import com.coddicted.buzzma.shared.exception.BusinessRuleViolationException;
import com.coddicted.buzzma.shared.exception.ForbiddenException;
import com.coddicted.buzzma.shared.exception.NotFoundException;
import com.coddicted.buzzma.storage.service.StorageService;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderServiceImpl extends BaseCrudService implements OrderService {

  private static final Logger LOGGER = LoggerFactory.getLogger(OrderServiceImpl.class);

  private final OrderRepository orderRepository;
  private final CampaignRepository campaignRepository;
  private final StorageService storageService;

  public OrderServiceImpl(
      final OrderRepository orderRepository,
      final CampaignRepository campaignRepository,
      final StorageService storageService) {
    this.orderRepository = orderRepository;
    this.campaignRepository = campaignRepository;
    this.storageService = storageService;
  }

  @Override
  @Transactional
  public Order createOrder(
      final Order order,
      final byte[] screenshot,
      final String screenshotFilename,
      final String contentType) {

    final Campaign campaign =
        this.campaignRepository
            .findByIdAndIsDeletedFalse(order.getCampaignId())
            .orElseThrow(
                () -> {
                  LOGGER.warn("Campaign not found for order creation: {}", order.getCampaignId());
                  return new NotFoundException("Campaign not found: " + order.getCampaignId());
                });

    if (campaign.getStatus() != CampaignStatus.CAMPAIGN_STATUS_ACTIVE) {
      LOGGER.warn("Campaign {} is not active, cannot create order", order.getCampaignId());
      throw new BusinessRuleViolationException("Campaign is not active");
    }

    if (this.orderRepository.existsByBuyerIdAndCampaignIdAndIsDeletedFalse(
        order.getUserId(), order.getCampaignId())) {
      LOGGER.warn(
          "Buyer {} already has an order for campaign {}",
          order.getUserId(),
          order.getCampaignId());
      throw new BusinessRuleViolationException("You have already claimed this deal");
    }

    final String screenshotKey =
        this.storageService.store("orders", screenshotFilename, contentType, screenshot);

    final Order toSave =
        order.toBuilder()
            .status(OrderWorkflowStatus.ORDERED)
            .orderScreenshotKey(screenshotKey)
            .isDeleted(false)
            .createdBy(order.getUserId())
            .updatedBy(order.getUserId())
            .build();

    return this.orderRepository.save(toSave);
  }

  @Override
  @Transactional
  public Order submitReview(
      final UUID orderId,
      final UUID buyerId,
      final String reviewUrl,
      final byte[] screenshot,
      final String filename,
      final String contentType) {

    final Order order = loadAndVerifyOwnership(orderId, buyerId);

    if (order.getStatus() != OrderWorkflowStatus.ORDERED) {
      LOGGER.warn(
          "Order {} in status {} cannot transition to PROOF_SUBMITTED", orderId, order.getStatus());
      throw new BusinessRuleViolationException(
          "Review can only be submitted when order is in ORDERED status");
    }

    final String screenshotKey =
        this.storageService.store("orders", filename, contentType, screenshot);

    final Order updated =
        order.toBuilder()
            .status(OrderWorkflowStatus.PROOF_SUBMITTED)
            .reviewUrl(reviewUrl)
            .reviewScreenshotKey(screenshotKey)
            .updatedBy(buyerId)
            .build();

    return this.orderRepository.save(updated);
  }

  @Override
  @Transactional
  public Order submitReturn(
      final UUID orderId,
      final UUID buyerId,
      final byte[] screenshot,
      final String filename,
      final String contentType) {

    final Order order = loadAndVerifyOwnership(orderId, buyerId);

    if (order.getStatus() != OrderWorkflowStatus.PROOF_SUBMITTED) {
      LOGGER.warn(
          "Order {} in status {} cannot transition to UNDER_REVIEW", orderId, order.getStatus());
      throw new BusinessRuleViolationException(
          "Return screenshot can only be submitted when order is in PROOF_SUBMITTED status");
    }

    final String screenshotKey =
        this.storageService.store("orders", filename, contentType, screenshot);

    final Order updated =
        order.toBuilder()
            .status(OrderWorkflowStatus.UNDER_REVIEW)
            .returnScreenshotKey(screenshotKey)
            .updatedBy(buyerId)
            .build();

    return this.orderRepository.save(updated);
  }

  @Override
  @Transactional(readOnly = true)
  public Order getById(final UUID orderId, final UUID userId) {
    return loadAndVerifyOwnership(orderId, userId);
  }

  @Override
  @Transactional(readOnly = true)
  public List<Order> listByBuyer(final UUID buyerId) {
    return this.orderRepository.findByBuyerIdAndIsDeletedFalse(buyerId);
  }

  private Order loadAndVerifyOwnership(final UUID orderId, final UUID userId) {
    final Order order =
        this.orderRepository
            .findByIdAndIsDeletedFalse(orderId)
            .orElseThrow(
                () -> {
                  LOGGER.warn("Order not found: {}", orderId);
                  return new NotFoundException("Order not found: " + orderId);
                });
    if (!order.getBuyerId().equals(buyerId)) {
      LOGGER.warn(
          "Buyer {} attempted to access order {} owned by {}",
          buyerId,
          orderId,
          order.getBuyerId());
      throw new ForbiddenException("Access denied");
    }
    return order;
  }
}
