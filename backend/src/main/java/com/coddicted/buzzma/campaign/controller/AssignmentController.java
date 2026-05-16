package com.coddicted.buzzma.campaign.controller;

import com.coddicted.buzzma.campaign.dto.PagedAssignmentsResponseDto;
import com.coddicted.buzzma.campaign.dto.PublishAssignmentRequestDto;
import com.coddicted.buzzma.campaign.entity.CampaignAssignmentStatus;
import com.coddicted.buzzma.campaign.mapper.AssignmentMapper;
import com.coddicted.buzzma.campaign.model.Assignment;
import com.coddicted.buzzma.campaign.service.AssignmentService;
import com.coddicted.buzzma.shared.security.CurrentUserId;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
  private final AssignmentMapper assignmentMapper;

  public AssignmentController(
      final AssignmentService assignmentService, final AssignmentMapper assignmentMapper) {
    this.assignmentService = assignmentService;
    this.assignmentMapper = assignmentMapper;
  }

  @GetMapping
  public PagedAssignmentsResponseDto getAssignments(
      @CurrentUserId final UUID requesterId,
      @RequestParam final CampaignAssignmentStatus status,
      @RequestParam(defaultValue = "0") final int page,
      @RequestParam(defaultValue = "20") final int size) {
    final Pageable pageable = PageRequest.of(page, size);
    final Page<Assignment> assignmentsPage =
        this.assignmentService.getAssignments(requesterId, status, pageable);
    return PagedAssignmentsResponseDto.builder()
        .items(this.assignmentMapper.toResponse(assignmentsPage.getContent()))
        .total(assignmentsPage.getTotalElements())
        .page(page)
        .totalPages(assignmentsPage.getTotalPages())
        .build();
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
        requesterId);
  }
}
