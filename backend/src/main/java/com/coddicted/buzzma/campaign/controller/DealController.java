package com.coddicted.buzzma.campaign.controller;

import com.coddicted.buzzma.campaign.dto.CampaignOptionDto;
import com.coddicted.buzzma.campaign.dto.DealResponseDto;
import com.coddicted.buzzma.campaign.dto.PagedDealsResponseDto;
import com.coddicted.buzzma.campaign.entity.Deal;
import com.coddicted.buzzma.campaign.mapper.DealMapper;
import com.coddicted.buzzma.campaign.service.DealService;
import com.coddicted.buzzma.connection.entity.ConnectionStatus;
import com.coddicted.buzzma.connection.service.ConnectionService;
import com.coddicted.buzzma.identity.entity.UserRole;
import com.coddicted.buzzma.identity.service.UserService;
import com.coddicted.buzzma.shared.exception.NotFoundException;
import com.coddicted.buzzma.shared.security.CurrentUserId;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/deals")
public class DealController {

  private final DealService dealService;
  private final ConnectionService connectionService;
  private final DealMapper dealMapper;
  private final UserService userService;

  public DealController(
      final DealService dealService,
      final ConnectionService connectionService,
      final DealMapper dealMapper,
      final UserService userService) {
    this.dealService = dealService;
    this.connectionService = connectionService;
    this.dealMapper = dealMapper;
    this.userService = userService;
  }

  @GetMapping("/active")
  @PreAuthorize(UserRole.Expr.BUYER)
  public PagedDealsResponseDto getActiveDeals(
      @CurrentUserId final UUID requesterId,
      @RequestParam(defaultValue = "0") final int page,
      @RequestParam(defaultValue = "20") final int size) {
    final Set<UUID> ownerIds =
        this.connectionService
            .getConnectionsByToUserIdAndStatus(
                requesterId, ConnectionStatus.CONNECTION_STATUS_ACCEPTED)
            .stream()
            .map(view -> view.getConnection().getFromUserId())
            .collect(Collectors.toSet());

    final Page<Deal> dealsPage =
        ownerIds.isEmpty()
            ? Page.empty(PageRequest.of(page, size))
            : this.dealService.getActiveDeals(ownerIds, requesterId, page, size);
    final Map<UUID, String> ownerNames = this.userService.getNamesByIds(ownerIds);
    final List<DealResponseDto> items =
        this.dealMapper.toDealResponse(dealsPage.getContent()).stream()
            .map(item -> item.toBuilder().ownerName(ownerNames.get(item.getOwnerId())).build())
            .toList();
    return PagedDealsResponseDto.builder()
        .items(items)
        .total(dealsPage.getTotalElements())
        .page(page)
        .totalPages(dealsPage.getTotalPages())
        .build();
  }

  @GetMapping("/active/{id}")
  @PreAuthorize(UserRole.Expr.BUYER)
  public DealResponseDto getActiveDealById(
      @CurrentUserId final UUID requesterId, @PathVariable final UUID id) {
    final Set<UUID> ownerIds =
        this.connectionService
            .getConnectionsByToUserIdAndStatus(
                requesterId, ConnectionStatus.CONNECTION_STATUS_ACCEPTED)
            .stream()
            .map(view -> view.getConnection().getFromUserId())
            .collect(Collectors.toSet());
    final Deal deal =
        this.dealService
            .getActiveDealById(id, ownerIds)
            .orElseThrow(() -> new NotFoundException("Deal not found: " + id));
    final Map<UUID, String> ownerNames = this.userService.getNamesByIds(ownerIds);
    return this.dealMapper.toDealResponse(deal).toBuilder()
        .ownerName(ownerNames.get(deal.getOwnerId()))
        .build();
  }

  @GetMapping("/campaigns")
  @PreAuthorize(UserRole.Expr.MEDIATOR)
  public List<CampaignOptionDto> getPublishedCampaigns(@CurrentUserId final UUID requesterId) {
    return this.dealService.getPublishedCampaigns(requesterId).stream()
        .map(
            c ->
                CampaignOptionDto.builder()
                    .id(c.getId())
                    .title(c.getTitle())
                    .code(c.getCode())
                    .build())
        .toList();
  }

  @GetMapping("/brands")
  @PreAuthorize(UserRole.Expr.MEDIATOR)
  public List<String> getPublishedBrandNames(@CurrentUserId final UUID requesterId) {
    return this.dealService.getPublishedBrandNames(requesterId);
  }

  @GetMapping("/by-code/{code}")
  @PreAuthorize(UserRole.Expr.MEDIATOR)
  public DealResponseDto getByCode(
      @CurrentUserId final UUID requesterId, @PathVariable final String code) {
    return this.dealMapper.toDealResponse(this.dealService.getByCodeForOwner(code, requesterId));
  }
}
