package com.coddicted.buzzma.order.controller;

import com.coddicted.buzzma.campaign.entity.Campaign;
import com.coddicted.buzzma.campaign.service.DealService;
import com.coddicted.buzzma.order.dto.OrderRequestDto;
import com.coddicted.buzzma.order.dto.OrderResponseDto;
import com.coddicted.buzzma.order.entity.Order;
import com.coddicted.buzzma.order.mapper.OrderMapper;
import com.coddicted.buzzma.order.service.OrderService;
import com.coddicted.buzzma.shared.security.CurrentUserId;
import jakarta.validation.Valid;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

  private final OrderService orderService;
  private final DealService dealService;
  private final OrderMapper orderMapper;

  public OrderController(
      final OrderService orderService,
      final DealService dealService,
      final OrderMapper orderMapper) {
    this.orderService = orderService;
    this.dealService = dealService;
    this.orderMapper = orderMapper;
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public OrderResponseDto create(
      @CurrentUserId final UUID requesterId, @Valid @RequestBody final OrderRequestDto request) {
    final MultipartFile screenshot = request.getScreenshot();
    final Order order =
        this.orderService.createOrder(
            this.orderMapper.toEntity(request, requesterId),
            readBytes(screenshot),
            screenshot.getOriginalFilename(),
            screenshot.getContentType());
    final Campaign campaign = this.dealService.getById(order.getCampaignId());
    return this.orderMapper.toResponse(order, campaign);
  }

  @PostMapping("/{id}/review")
  public OrderResponseDto submitReview(
      @CurrentUserId final UUID requesterId,
      @PathVariable final UUID id,
      @RequestParam(required = false) final String reviewUrl,
      @RequestParam("screenshot") final MultipartFile screenshot) {
    final Order order =
        this.orderService.submitReview(
            id,
            requesterId,
            reviewUrl,
            readBytes(screenshot),
            screenshot.getOriginalFilename(),
            screenshot.getContentType());
    final Campaign campaign = this.dealService.getById(order.getCampaignId());
    return this.orderMapper.toResponse(order, campaign);
  }

  @PostMapping("/{id}/return")
  public OrderResponseDto submitReturn(
      @CurrentUserId final UUID requesterId,
      @PathVariable final UUID id,
      @RequestParam("screenshot") final MultipartFile screenshot) {
    final Order order =
        this.orderService.submitReturn(
            id,
            requesterId,
            readBytes(screenshot),
            screenshot.getOriginalFilename(),
            screenshot.getContentType());
    final Campaign campaign = this.dealService.getById(order.getCampaignId());
    return this.orderMapper.toResponse(order, campaign);
  }

  @GetMapping
  public List<OrderResponseDto> list(@CurrentUserId final UUID requesterId) {
    return this.orderService.listByBuyer(requesterId).stream()
        .map(
            order ->
                this.orderMapper.toResponse(order, this.dealService.getById(order.getCampaignId())))
        .toList();
  }

  @GetMapping("/{id}")
  public OrderResponseDto getById(
      @CurrentUserId final UUID requesterId, @PathVariable final UUID id) {
    final Order order = this.orderService.getById(id, requesterId);
    final Campaign campaign = this.dealService.getById(order.getCampaignId());
    return this.orderMapper.toResponse(order, campaign);
  }

  private byte[] readBytes(final MultipartFile file) {
    try {
      return file.getBytes();
    } catch (final IOException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Failed to read uploaded file");
    }
  }
}
