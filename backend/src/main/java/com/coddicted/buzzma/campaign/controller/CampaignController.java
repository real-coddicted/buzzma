package com.coddicted.buzzma.campaign.controller;

import com.coddicted.buzzma.campaign.dto.AssignableCampaignResponseDto;
import com.coddicted.buzzma.campaign.dto.CampaignBatchRequestDto;
import com.coddicted.buzzma.campaign.dto.CampaignBriefDto;
import com.coddicted.buzzma.campaign.dto.CampaignOptionDto;
import com.coddicted.buzzma.campaign.dto.CampaignRequestDto;
import com.coddicted.buzzma.campaign.dto.CampaignResponseDto;
import com.coddicted.buzzma.campaign.dto.CampaignSearchRequestDto;
import com.coddicted.buzzma.campaign.dto.CampaignStepDto;
import com.coddicted.buzzma.campaign.dto.PagedCampaignsResponseDto;
import com.coddicted.buzzma.campaign.dto.ShareCampaignRequestDto;
import com.coddicted.buzzma.campaign.dto.ShareCampaignResponseDto;
import com.coddicted.buzzma.campaign.dto.ShareableCampaignResponseDto;
import com.coddicted.buzzma.campaign.dto.SharedCampaignResponseDto;
import com.coddicted.buzzma.campaign.dto.SharedCampaignViewResponseDto;
import com.coddicted.buzzma.campaign.entity.CampaignAction;
import com.coddicted.buzzma.campaign.mapper.CampaignMapper;
import com.coddicted.buzzma.campaign.mapper.CampaignTypeStepMapper;
import com.coddicted.buzzma.campaign.mapper.ShareableCampaignMapper;
import com.coddicted.buzzma.campaign.mapper.SharedCampaignMapper;
import com.coddicted.buzzma.campaign.mapper.SharedCampaignSummaryMapper;
import com.coddicted.buzzma.campaign.model.CampaignSearchCriteria;
import com.coddicted.buzzma.campaign.model.CampaignSummary;
import com.coddicted.buzzma.campaign.processor.CampaignProcessor;
import com.coddicted.buzzma.campaign.service.CampaignService;
import com.coddicted.buzzma.campaign.service.CampaignTypeStepService;
import com.coddicted.buzzma.identity.entity.UserRole;
import com.coddicted.buzzma.shared.security.CurrentUserId;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/campaigns")
@Validated
public class CampaignController {

  private final CampaignService service;
  private final CampaignMapper campaignMapper;
  private final CampaignTypeStepMapper campaignTypeStepMapper;
  private final CampaignProcessor campaignProcessor;
  private final CampaignTypeStepService campaignTypeStepService;
  private final ShareableCampaignMapper shareableCampaignMapper;
  private final SharedCampaignMapper sharedCampaignMapper;
  private final SharedCampaignSummaryMapper sharedCampaignSummaryMapper;

  public CampaignController(
      final CampaignService service,
      final CampaignMapper campaignMapper,
      final CampaignTypeStepMapper campaignTypeStepMapper,
      final CampaignProcessor campaignProcessor,
      final CampaignTypeStepService campaignTypeStepService,
      final ShareableCampaignMapper shareableCampaignMapper,
      final SharedCampaignMapper sharedCampaignMapper,
      final SharedCampaignSummaryMapper sharedCampaignSummaryMapper) {
    this.service = service;
    this.campaignMapper = campaignMapper;
    this.campaignTypeStepMapper = campaignTypeStepMapper;
    this.campaignProcessor = campaignProcessor;
    this.campaignTypeStepService = campaignTypeStepService;
    this.shareableCampaignMapper = shareableCampaignMapper;
    this.sharedCampaignMapper = sharedCampaignMapper;
    this.sharedCampaignSummaryMapper = sharedCampaignSummaryMapper;
  }

  @GetMapping("/step-config")
  public Map<String, List<CampaignStepDto>> getStepConfig() {
    return this.campaignTypeStepMapper.toCampaignStepDtoMap(
        this.campaignTypeStepService.getStepConfig());
  }

  @GetMapping
  public PagedCampaignsResponseDto list(
      @CurrentUserId final UUID requesterId,
      @RequestParam(defaultValue = "0") final int page,
      @RequestParam(defaultValue = "10") final int size) {
    final Page<CampaignSummary> result =
        this.service.getByOwnerId(requesterId, PageRequest.of(page, size));
    return PagedCampaignsResponseDto.builder()
        .items(this.campaignMapper.toSummaries(result.getContent()))
        .total(result.getTotalElements())
        .page(page)
        .totalPages(result.getTotalPages())
        .build();
  }

  @GetMapping("/{id}")
  public CampaignResponseDto getById(@PathVariable final UUID id) {
    return this.campaignProcessor.getById(id);
  }

  /** Bulk lookup for display purposes (e.g. campaign details on the payout claims list). */
  @PostMapping("/batch")
  public List<CampaignBriefDto> getByIds(@RequestBody final CampaignBatchRequestDto request) {
    final Set<UUID> requestedIds = request.getIds() == null ? Set.of() : request.getIds();
    if (requestedIds.isEmpty()) {
      return List.of();
    }
    return this.service.findCampaignsById(requestedIds).stream()
        .map(this.campaignMapper::toBrief)
        .toList();
  }

  @GetMapping("/assignable")
  @PreAuthorize(UserRole.Expr.AGENCY)
  public List<AssignableCampaignResponseDto> getAssignableCampaigns(
      @CurrentUserId final UUID requesterId, @RequestParam final UUID assigneeId) {
    return this.service.findAssignableCampaigns(requesterId, assigneeId);
  }

  @GetMapping("/shareable")
  @PreAuthorize(UserRole.Expr.AGENCY)
  public List<ShareableCampaignResponseDto> getShareableCampaigns(
      @CurrentUserId final UUID requesterId) {
    return this.shareableCampaignMapper.toResponse(
        this.service.findShareableCampaigns(requesterId));
  }

  @GetMapping("/shared")
  @PreAuthorize(UserRole.Expr.BRAND)
  public List<SharedCampaignResponseDto> getSharedCampaigns(@CurrentUserId final UUID requesterId) {
    return this.sharedCampaignMapper.toResponse(this.service.findSharedCampaigns(requesterId));
  }

  @GetMapping("/shared-by-me")
  @PreAuthorize(UserRole.Expr.AGENCY)
  public List<SharedCampaignViewResponseDto> getCampaignsSharedByMe(
      @CurrentUserId final UUID requesterId) {
    return this.sharedCampaignSummaryMapper.toResponse(
        this.service.findCampaignsSharedByOwner(requesterId));
  }

  @PostMapping("/{campaignId}/share")
  @PreAuthorize(UserRole.Expr.AGENCY)
  public ShareCampaignResponseDto shareCampaign(
      @CurrentUserId final UUID requesterId,
      @PathVariable final UUID campaignId,
      @Valid @RequestBody final ShareCampaignRequestDto request) {
    return this.campaignProcessor.shareCampaign(requesterId, campaignId, request.getToUserId());
  }

  @GetMapping("/brands")
  @PreAuthorize(UserRole.Expr.AGENCY + UserRole.Expr.OR + UserRole.Expr.BRAND)
  public List<String> getBrandNames(@CurrentUserId final UUID requesterId) {
    return this.service.getBrandNames(requesterId);
  }

  @GetMapping("/names")
  @PreAuthorize(UserRole.Expr.AGENCY + UserRole.Expr.OR + UserRole.Expr.BRAND)
  public List<CampaignOptionDto> getCampaignNames(@CurrentUserId final UUID requesterId) {
    return this.service.getCampaignsForOwner(requesterId).stream()
        .map(
            c ->
                CampaignOptionDto.builder()
                    .id(c.getId())
                    .title(c.getTitle())
                    .code(c.getCode())
                    .build())
        .toList();
  }

  @PostMapping("/search")
  @PreAuthorize(UserRole.Expr.AGENCY + UserRole.Expr.OR + UserRole.Expr.BRAND)
  public PagedCampaignsResponseDto search(
      @CurrentUserId final UUID requesterId,
      @RequestBody final CampaignSearchRequestDto request,
      @RequestParam(defaultValue = "0") final int page,
      @RequestParam(defaultValue = "20") final int size) {
    final Pageable pageable = PageRequest.of(page, size);
    final CampaignSearchCriteria criteria =
        new CampaignSearchCriteria(
            request.getBrands(),
            request.getPlatforms(),
            request.getTypes(),
            request.getStatuses(),
            request.getFromDate(),
            request.getToDate());
    final Page<CampaignSummary> result = this.service.search(requesterId, criteria, pageable);
    return PagedCampaignsResponseDto.builder()
        .items(this.campaignMapper.toSummaries(result.getContent()))
        .total(result.getTotalElements())
        .page(page)
        .totalPages(result.getTotalPages())
        .build();
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @PreAuthorize(UserRole.Expr.AGENCY + UserRole.Expr.OR + UserRole.Expr.BRAND)
  public CampaignResponseDto create(
      @CurrentUserId final UUID requesterId, @Valid @RequestBody final CampaignRequestDto request) {
    return this.campaignProcessor.create(requesterId, request);
  }

  @PatchMapping("/{id}")
  @PreAuthorize(UserRole.Expr.AGENCY + UserRole.Expr.OR + UserRole.Expr.BRAND)
  public CampaignResponseDto updateCampaign(
      @CurrentUserId final UUID requesterId,
      @PathVariable final UUID id,
      @Valid @RequestBody final CampaignRequestDto request) {
    return this.campaignProcessor.updateCampaign(requesterId, id, request);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @PreAuthorize(UserRole.Expr.AGENCY + UserRole.Expr.OR + UserRole.Expr.BRAND)
  public void delete(@CurrentUserId final UUID requesterId, @PathVariable final UUID id) {
    this.service.delete(id, requesterId);
  }

  @PostMapping("/{id}/action/{action}")
  public CampaignResponseDto action(
      @PathVariable final UUID id,
      @PathVariable final CampaignAction action,
      @CurrentUserId final UUID requesterId) {
    return this.campaignMapper.toResponse(this.service.action(id, action, requesterId));
  }

  @PostMapping("/{id}/copy")
  public CampaignResponseDto copy(
      @PathVariable final UUID id, @CurrentUserId final UUID requesterId) {
    return this.campaignMapper.toResponse(this.service.copy(id, requesterId));
  }
}
