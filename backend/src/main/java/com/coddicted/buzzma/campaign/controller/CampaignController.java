package com.coddicted.buzzma.campaign.controller;

import com.coddicted.buzzma.campaign.dto.CampaignRequestDto;
import com.coddicted.buzzma.campaign.dto.CampaignResponseDto;
import com.coddicted.buzzma.campaign.dto.CampaignSummaryResponseDto;
import com.coddicted.buzzma.campaign.entity.CampaignAction;
import com.coddicted.buzzma.campaign.mapper.CampaignMapper;
import com.coddicted.buzzma.campaign.processor.CampaignProcessor;
import com.coddicted.buzzma.campaign.service.CampaignService;
import com.coddicted.buzzma.shared.security.CurrentUserId;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/campaigns")
@Validated
public class CampaignController {

  private final CampaignService service;
  private final CampaignMapper campaignMapper;
  private final CampaignProcessor campaignProcessor;

  public CampaignController(
      final CampaignService service,
      final CampaignMapper campaignMapper,
      final CampaignProcessor campaignProcessor) {
    this.service = service;
    this.campaignMapper = campaignMapper;
    this.campaignProcessor = campaignProcessor;
  }

  @GetMapping
  public List<CampaignSummaryResponseDto> list(@CurrentUserId final UUID requesterId) {
    return this.campaignMapper.toSummaries(this.service.getByOwnerId(requesterId));
  }

  @GetMapping("/{id}")
  public CampaignResponseDto getById(@PathVariable final UUID id) {
    return this.campaignProcessor.getById(id);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public CampaignResponseDto create(
      @CurrentUserId final UUID requesterId, @Valid @RequestBody final CampaignRequestDto request) {
    return this.campaignProcessor.create(requesterId, request);
  }

  @PatchMapping("/{id}")
  public CampaignResponseDto update(
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
