package com.coddicted.buzzma.campaign.controller;

import com.coddicted.buzzma.campaign.dto.PagedDealsResponseDto;
import com.coddicted.buzzma.campaign.entity.Deal;
import com.coddicted.buzzma.campaign.mapper.DealMapper;
import com.coddicted.buzzma.campaign.service.DealService;
import com.coddicted.buzzma.shared.security.CurrentUserId;
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

  @GetMapping("/unclaimed")
  public PagedDealsResponseDto getUnclaimedDeals(
      @CurrentUserId final UUID requesterId,
      @RequestParam final UUID ownerId,
      @RequestParam(defaultValue = "0") final int page,
      @RequestParam(defaultValue = "20") final int size) {
    // TODO need to remove ownerId as well from the request.
    //  It should be determined based on the current security-context
    final Page<Deal> dealsPage =
        this.dealService.getUnclaimedDeals(ownerId, requesterId, page, size);
    return PagedDealsResponseDto.builder()
        .items(this.dealMapper.toDealResponse(dealsPage.getContent()))
        .total(dealsPage.getTotalElements())
        .page(page)
        .totalPages(dealsPage.getTotalPages())
        .build();
  }
}
