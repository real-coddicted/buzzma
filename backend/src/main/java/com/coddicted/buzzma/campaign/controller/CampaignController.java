package com.coddicted.buzzma.campaign.controller;

import com.coddicted.buzzma.campaign.dto.CampaignRequestDto;
import com.coddicted.buzzma.campaign.dto.CampaignResponseDto;
import com.coddicted.buzzma.campaign.dto.CampaignSearchRequestDto;
import com.coddicted.buzzma.campaign.dto.CampaignStepDto;
import com.coddicted.buzzma.campaign.dto.PagedCampaignsResponseDto;
import com.coddicted.buzzma.campaign.entity.CampaignAction;
import com.coddicted.buzzma.campaign.mapper.CampaignMapper;
import com.coddicted.buzzma.campaign.mapper.CampaignTypeStepMapper;
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

  public CampaignController(
      final CampaignService service,
      final CampaignMapper campaignMapper,
      final CampaignTypeStepMapper campaignTypeStepMapper,
      final CampaignProcessor campaignProcessor,
      final CampaignTypeStepService campaignTypeStepService) {
    this.service = service;
    this.campaignMapper = campaignMapper;
    this.campaignTypeStepMapper = campaignTypeStepMapper;
    this.campaignProcessor = campaignProcessor;
    this.campaignTypeStepService = campaignTypeStepService;
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

  @GetMapping("/brands")
  @PreAuthorize(UserRole.Expr.AGENCY + UserRole.Expr.OR + UserRole.Expr.BRAND)
  public List<String> getBrandNames(@CurrentUserId final UUID requesterId) {
    return this.service.getBrandNames(requesterId);
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
  public void delete(@PathVariable final UUID id, @CurrentUserId final UUID requesterId) {
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
