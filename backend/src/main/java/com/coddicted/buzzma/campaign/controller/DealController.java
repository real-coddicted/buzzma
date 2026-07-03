package com.coddicted.buzzma.campaign.controller;

import com.coddicted.buzzma.campaign.dto.PagedDealsResponseDto;
import com.coddicted.buzzma.campaign.entity.Deal;
import com.coddicted.buzzma.campaign.mapper.DealMapper;
import com.coddicted.buzzma.campaign.service.DealService;
import com.coddicted.buzzma.connection.entity.Connection;
import com.coddicted.buzzma.connection.entity.ConnectionStatus;
import com.coddicted.buzzma.connection.service.ConnectionService;
import com.coddicted.buzzma.shared.security.CurrentUserId;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/deals")
public class DealController {

  private final DealService dealService;
  private final ConnectionService connectionService;
  private final DealMapper dealMapper;

  public DealController(
      final DealService dealService,
      final ConnectionService connectionService,
      final DealMapper dealMapper) {
    this.dealService = dealService;
    this.connectionService = connectionService;
    this.dealMapper = dealMapper;
  }

  @GetMapping("/active")
  @PreAuthorize("hasAnyRole('BUYER')")
  public PagedDealsResponseDto getActiveDeals(
      @CurrentUserId final UUID requesterId,
      @RequestParam(defaultValue = "0") final int page,
      @RequestParam(defaultValue = "20") final int size) {
    final Connection connection =
        this.connectionService.getConnectionByToUserIdAndStatus(
            requesterId, ConnectionStatus.CONNECTION_STATUS_ACCEPTED);

    final Page<Deal> dealsPage =
        this.dealService.getActiveDeals(connection.getFromUserId(), requesterId, page, size);
    return PagedDealsResponseDto.builder()
        .items(this.dealMapper.toDealResponse(dealsPage.getContent()))
        .total(dealsPage.getTotalElements())
        .page(page)
        .totalPages(dealsPage.getTotalPages())
        .build();
  }
}
