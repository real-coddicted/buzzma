package com.coddicted.buzzma.campaign.controller;

import com.coddicted.buzzma.campaign.dto.AssignmentResponseDto;
import com.coddicted.buzzma.campaign.dto.PagedAssignmentsResponseDto;
import com.coddicted.buzzma.campaign.mapper.DealMapper;
import com.coddicted.buzzma.campaign.service.AssignmentService;
import com.coddicted.buzzma.shared.security.CurrentUserId;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/assignments")
public class AssignmentController {

  private final AssignmentService dealService;
  private final DealMapper dealMapper;

  public AssignmentController(final AssignmentService dealService, final DealMapper dealMapper) {
    this.dealService = dealService;
    this.dealMapper = dealMapper;
  }

  @GetMapping("")
  public PagedAssignmentsResponseDto getAssignments(
          @CurrentUserId final UUID requesterId,

          @RequestParam(defaultValue = "ASSIGNMENT_STATUS_PUBLISHED") final AssignmentStatus status,
          @RequestParam(defaultValue = "0") final int page,
          @RequestParam(defaultValue = "20") final int size) {
    final Page<Assignment> result = this.dealService.getDeals(requesterId, status, page, size);
    final List<AssignmentResponseDto> items =
        result.getContent().stream().map(this.dealMapper::toResponse).toList();
    return PagedAssignmentsResponseDto.builder()
        .items(items)
        .total(result.getTotalElements())
        .page(result.getNumber())
        .totalPages(result.getTotalPages())
        .build();
  }
}
