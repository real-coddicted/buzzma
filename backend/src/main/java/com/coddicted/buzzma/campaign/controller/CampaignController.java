package com.coddicted.buzzma.campaign.controller;

import com.coddicted.buzzma.campaign.dto.CampaignRequestDto;
import com.coddicted.buzzma.campaign.dto.CampaignResponseDto;
import com.coddicted.buzzma.campaign.entity.CampaignAction;
import com.coddicted.buzzma.campaign.mapper.CampaignMapper;
import com.coddicted.buzzma.campaign.service.CampaignService;
import com.coddicted.buzzma.shared.security.CurrentUserId;
import jakarta.validation.Valid;
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

  public CampaignController(final CampaignService service, final CampaignMapper campaignMapper) {
    this.service = service;
    this.campaignMapper = campaignMapper;
  }

  @GetMapping("/{id}")
  public CampaignResponseDto getById(@PathVariable final UUID id) {
    return this.campaignMapper.toResponse(this.service.getById(id));
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public CampaignResponseDto create(@Valid @RequestBody final CampaignRequestDto request) {
    return this.campaignMapper.toResponse(this.service.create(this.campaignMapper.toCampaignEntity(request)));
  }

  @PatchMapping("/{id}")
  public CampaignResponseDto update(
      @PathVariable final UUID id, @Valid @RequestBody final CampaignRequestDto request) {
    final var existing = this.service.getById(id);
      this.campaignMapper.updateCampaign(request, existing);
    return this.campaignMapper.toResponse(this.service.update(existing));
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
}
