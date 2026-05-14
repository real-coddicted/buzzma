package com.coddicted.buzzma.order.controller;

import com.coddicted.buzzma.campaign.entity.Campaign;
import com.coddicted.buzzma.campaign.entity.CampaignType;
import com.coddicted.buzzma.order.mapper.DealMapper;
import com.coddicted.buzzma.order.service.DealService;
import com.coddicted.buzzma.shared.enums.Platform;
import com.coddicted.buzzma.shared.security.CurrentUserId;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/deals")
public class DealController {

  private final DealService dealService;
  private final DealMapper dealMapper;

  public DealController(final DealService dealService, final DealMapper dealMapper) {
    this.dealService = dealService;
    this.dealMapper = dealMapper;
  }

  @GetMapping
  public PagedDealsResponseDto getDeals(
      @CurrentUserId final UUID requesterId,
      @RequestParam(defaultValue = "0") final int page,
      @RequestParam(defaultValue = "20") final int size,
      @RequestParam(required = false) final CampaignType type,
      @RequestParam(required = false) final Platform platform) {
    final Page<Campaign> result = this.dealService.getDeals(page, size, type, platform);
    final List<DealResponseDto> items =
        result.getContent().stream().map(this.dealMapper::toResponse).toList();
    return PagedDealsResponseDto.builder()
        .items(items)
        .total(result.getTotalElements())
        .page(result.getNumber())
        .totalPages(result.getTotalPages())
        .build();
  }
}
