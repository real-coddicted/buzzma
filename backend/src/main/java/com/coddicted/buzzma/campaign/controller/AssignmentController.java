package com.coddicted.buzzma.campaign.controller;

import com.coddicted.buzzma.campaign.dto.AssignToMediatorRequestDto;
import com.coddicted.buzzma.campaign.dto.AssignmentResponseDto;
import com.coddicted.buzzma.campaign.dto.AssignmentSummaryResponseDto;
import com.coddicted.buzzma.campaign.dto.CampaignAssignmentResponseDto;
import com.coddicted.buzzma.campaign.dto.CommissionResponseDto;
import com.coddicted.buzzma.campaign.dto.PagedAssignmentsResponseDto;
import com.coddicted.buzzma.campaign.dto.PublishAssignmentRequestDto;
import com.coddicted.buzzma.campaign.entity.CampaignAssignmentStatus;
import com.coddicted.buzzma.campaign.entity.Commission;
import com.coddicted.buzzma.campaign.mapper.AssignmentMapper;
import com.coddicted.buzzma.campaign.mapper.CampaignAssignmentMapper;
import com.coddicted.buzzma.campaign.mapper.CommissionMapper;
import com.coddicted.buzzma.campaign.processor.CampaignAssignmentProcessor;
import com.coddicted.buzzma.campaign.service.CommissionService;
import com.coddicted.buzzma.identity.entity.UserRole;
import com.coddicted.buzzma.identity.service.UserService;
import com.coddicted.buzzma.shared.security.CurrentUserId;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/assignments")
public class AssignmentController {

  private final CampaignAssignmentProcessor campaignAssignmentProcessor;
  private final CommissionService commissionService;
  private final AssignmentMapper assignmentMapper;
  private final CampaignAssignmentMapper campaignAssignmentMapper;
  private final CommissionMapper commissionMapper;
  private final UserService userService;

  public AssignmentController(
      final CampaignAssignmentProcessor campaignAssignmentProcessor,
      final CommissionService commissionService,
      final AssignmentMapper assignmentMapper,
      final CampaignAssignmentMapper campaignAssignmentMapper,
      final CommissionMapper commissionMapper,
      final UserService userService) {
    this.campaignAssignmentProcessor = campaignAssignmentProcessor;
    this.commissionService = commissionService;
    this.assignmentMapper = assignmentMapper;
    this.campaignAssignmentMapper = campaignAssignmentMapper;
    this.commissionMapper = commissionMapper;
    this.userService = userService;
  }

  @GetMapping
  public PagedAssignmentsResponseDto getAssignments(
      @CurrentUserId final UUID requesterId,
      @RequestParam final CampaignAssignmentStatus status,
      @RequestParam(defaultValue = "0") final int page,
      @RequestParam(defaultValue = "20") final int size) {
    final Pageable pageable = PageRequest.of(page, size);
    final Page<AssignmentSummaryResponseDto> assignmentsPage =
        this.campaignAssignmentProcessor.getAssignmentSummaries(requesterId, status, pageable);
    final Set<UUID> agencyIds =
        assignmentsPage.getContent().stream()
            .map(AssignmentSummaryResponseDto::getAgencyId)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
    final Map<UUID, String> agencyNames = this.userService.getNamesByIds(agencyIds);
    final List<AssignmentSummaryResponseDto> items =
        assignmentsPage.getContent().stream()
            .map(item -> item.toBuilder().agencyName(agencyNames.get(item.getAgencyId())).build())
            .toList();
    return PagedAssignmentsResponseDto.builder()
        .items(items)
        .total(assignmentsPage.getTotalElements())
        .page(page)
        .totalPages(assignmentsPage.getTotalPages())
        .build();
  }

  @GetMapping("/{id}")
  @PreAuthorize(UserRole.Expr.AGENCY + UserRole.Expr.OR + UserRole.Expr.MEDIATOR)
  public AssignmentResponseDto getAssignmentById(
      @CurrentUserId final UUID requesterId, @PathVariable final UUID id) {
    final AssignmentResponseDto dto =
        this.assignmentMapper.toResponse(
            this.campaignAssignmentProcessor.getAssignmentById(id, requesterId));
    final Set<UUID> agencyIds = dto.getAgencyId() != null ? Set.of(dto.getAgencyId()) : Set.of();
    final Map<UUID, String> agencyNames = this.userService.getNamesByIds(agencyIds);
    return dto.toBuilder().agencyName(agencyNames.get(dto.getAgencyId())).build();
  }

  @PostMapping("/{id}/publish")
  public boolean publishAssignment(
      @CurrentUserId final UUID requesterId,
      @PathVariable final UUID id,
      @Valid @RequestBody final PublishAssignmentRequestDto request) {

    return this.campaignAssignmentProcessor.publishAssignment(
        request.getCampaignId(),
        id,
        request.getCommissionChargedPaise(),
        request.getDealPricePaise(),
        requesterId,
        request.getAffiliateUrl(),
        request.isSendNotificationOnPublish());
  }

  @PostMapping("/assign-to-mediator")
  @PreAuthorize(UserRole.Expr.AGENCY)
  public List<CampaignAssignmentResponseDto> assignToMediator(
      @CurrentUserId final UUID requesterId,
      @Valid @RequestBody final AssignToMediatorRequestDto request) {
    return this.campaignAssignmentMapper.toResponse(
        this.campaignAssignmentProcessor.assignCampaignsToMediator(requesterId, request));
  }

  @GetMapping("/commissionCharged/{campaignId}")
  public CommissionResponseDto getCommissionCharged(
      @CurrentUserId final UUID requesterId, @PathVariable final UUID campaignId) {
    final Commission commission =
        this.commissionService.getCommissionCharged(campaignId, requesterId);
    return this.commissionMapper.toResponse(commission);
  }
}
