package com.coddicted.buzzma.campaign.controller;

import com.coddicted.buzzma.campaign.api.CampaignRequestDto;
import com.coddicted.buzzma.campaign.api.CampaignResponseDto;
import com.coddicted.buzzma.campaign.entity.CampaignAction;
import com.coddicted.buzzma.campaign.service.CampaignService;
import com.coddicted.buzzma.shared.security.CurrentUserId;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/campaigns")
@Validated
public class CampaignController {

    private final CampaignService service;

    public CampaignController(final CampaignService service) {
        this.service = service;
    }

    @GetMapping("/{id}")
    public CampaignResponseDto getById(@PathVariable UUID id) {
        return service.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CampaignResponseDto create(@Valid @RequestBody final CampaignRequestDto request) {
        return service.create(request);
    }

    @PatchMapping("/{id}")
    public CampaignResponseDto update(
            @PathVariable final UUID id, @Valid @RequestBody final CampaignRequestDto request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable final UUID id) {
        service.delete(id);
    }

    @PostMapping("/{id}/action/{action}")
    public CampaignResponseDto action(
            @PathVariable final UUID id,
            @PathVariable final CampaignAction action,
            @CurrentUserId final UUID requesterId) {
        return service.action(id, action, requesterId);
    }
}
