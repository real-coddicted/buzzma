package com.coddicted.buzzma.campaign.controller;

import com.coddicted.buzzma.campaign.dto.AssignmentResponseDto;
import com.coddicted.buzzma.campaign.dto.AssignmentSummaryResponseDto;
import com.coddicted.buzzma.campaign.dto.CommissionResponseDto;
import com.coddicted.buzzma.campaign.dto.PagedAssignmentsResponseDto;
import com.coddicted.buzzma.campaign.dto.PublishAssignmentRequestDto;
import com.coddicted.buzzma.campaign.entity.CampaignAssignmentStatus;
import com.coddicted.buzzma.campaign.entity.Commission;
import com.coddicted.buzzma.campaign.mapper.AssignmentMapper;
import com.coddicted.buzzma.campaign.mapper.CommissionMapper;
import com.coddicted.buzzma.campaign.service.AssignmentService;
import com.coddicted.buzzma.campaign.service.CommissionService;
import com.coddicted.buzzma.identity.entity.UserRole;
import com.coddicted.buzzma.shared.security.CurrentUserId;
import jakarta.validation.Valid;
import java.util.UUID;
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

  private final AssignmentService assignmentService;
  private final CommissionService commissionService;
  private final AssignmentMapper assignmentMapper;
  private final CommissionMapper commissionMapper;

  public AssignmentController(
      final AssignmentService assignmentService,
      final CommissionService commissionService,
      final AssignmentMapper assignmentMapper,
      final CommissionMapper commissionMapper) {
    this.assignmentService = assignmentService;
    this.commissionService = commissionService;
    this.assignmentMapper = assignmentMapper;
    this.commissionMapper = commissionMapper;
  }

  @GetMapping
  public PagedAssignmentsResponseDto getAssignments(
      @CurrentUserId final UUID requesterId,
      @RequestParam final CampaignAssignmentStatus status,
      @RequestParam(defaultValue = "0") final int page,
      @RequestParam(defaultValue = "20") final int size) {
    final Pageable pageable = PageRequest.of(page, size);
    final Page<AssignmentSummaryResponseDto> assignmentsPage =
        this.assignmentService.getAssignmentSummaries(requesterId, status, pageable);
    return PagedAssignmentsResponseDto.builder()
        .items(assignmentsPage.getContent())
        .total(assignmentsPage.getTotalElements())
        .page(page)
        .totalPages(assignmentsPage.getTotalPages())
        .build();
  }

  @GetMapping("/{id}")
  @PreAuthorize(UserRole.Expr.AGENCY + UserRole.Expr.OR + UserRole.Expr.MEDIATOR)
  public AssignmentResponseDto getAssignmentById(
      @CurrentUserId final UUID requesterId, @PathVariable final UUID id) {
    return this.assignmentMapper.toResponse(
        this.assignmentService.getAssignmentById(id, requesterId));
  }

  @PostMapping("/{id}/publish")
  public boolean publishAssignment(
      @CurrentUserId final UUID requesterId,
      @PathVariable final UUID id,
      @Valid @RequestBody final PublishAssignmentRequestDto request) {

    return this.assignmentService.publishAssignment(
        request.getCampaignId(),
        id,
        request.getCommissionChargedPaise(),
        request.getDealPricePaise(),
        requesterId,
        request.getAffiliateUrl());
  }

  @GetMapping("/commissionCharged/{campaignId}")
  public CommissionResponseDto getCommissionCharged(
      @CurrentUserId final UUID requesterId, @PathVariable final UUID campaignId) {
    final Commission commission =
        this.commissionService.getCommissionCharged(campaignId, requesterId);
    return this.commissionMapper.toResponse(commission);
  }
}
