package com.coddicted.buzzma.campaign.processor;

import com.coddicted.buzzma.campaign.dto.CampaignAssignmentRequestDto;
import com.coddicted.buzzma.campaign.dto.CampaignAssignmentResponseDto;
import com.coddicted.buzzma.campaign.dto.CampaignRequestDto;
import com.coddicted.buzzma.campaign.dto.CampaignResponseDto;
import com.coddicted.buzzma.campaign.dto.ShareCampaignResponseDto;
import com.coddicted.buzzma.campaign.entity.Campaign;
import com.coddicted.buzzma.campaign.entity.CampaignAction;
import com.coddicted.buzzma.campaign.entity.CampaignAssignment;
import com.coddicted.buzzma.campaign.entity.CampaignShare;
import com.coddicted.buzzma.campaign.entity.CampaignSlot;
import com.coddicted.buzzma.campaign.entity.CampaignStatus;
import com.coddicted.buzzma.campaign.entity.CampaignStepType;
import com.coddicted.buzzma.campaign.entity.Product;
import com.coddicted.buzzma.campaign.mapper.CampaignMapper;
import com.coddicted.buzzma.campaign.notification.CampaignEventPublisher;
import com.coddicted.buzzma.campaign.service.CampaignAssignmentService;
import com.coddicted.buzzma.campaign.service.CampaignService;
import com.coddicted.buzzma.campaign.service.CampaignShareService;
import com.coddicted.buzzma.campaign.service.CampaignSlotService;
import com.coddicted.buzzma.connection.service.ConnectionService;
import com.coddicted.buzzma.identity.service.UserService;
import com.coddicted.buzzma.shared.exception.BusinessRuleViolationException;
import com.coddicted.buzzma.shared.util.DateTimeUtils;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class CampaignProcessor {

  private final CampaignService service;
  private final CampaignMapper campaignMapper;
  private final ProductProcessor productProcessor;
  private final CampaignAssignmentService campaignAssignmentService;
  private final CampaignSlotService campaignSlotService;
  private final CampaignEventPublisher campaignEventPublisher;
  private final ConnectionService connectionService;
  private final UserService userService;
  private final CampaignShareService campaignShareService;

  public CampaignProcessor(
      final CampaignService service,
      final CampaignMapper campaignMapper,
      final ProductProcessor productProcessor,
      final CampaignAssignmentService campaignAssignmentService,
      final CampaignSlotService campaignSlotService,
      final CampaignEventPublisher campaignEventPublisher,
      final ConnectionService connectionService,
      final UserService userService,
      final CampaignShareService campaignShareService) {
    this.service = service;
    this.campaignMapper = campaignMapper;
    this.productProcessor = productProcessor;
    this.campaignAssignmentService = campaignAssignmentService;
    this.campaignSlotService = campaignSlotService;
    this.campaignEventPublisher = campaignEventPublisher;
    this.connectionService = connectionService;
    this.userService = userService;
    this.campaignShareService = campaignShareService;
  }

  @Transactional
  public ShareCampaignResponseDto shareCampaign(
      final UUID requesterId, final UUID campaignId, final UUID toUserId) {
    // The end-to-end flow this supports:
    // a. agency gives an invite code to the brand
    // b. brand uses that invite code to register
    // c. agency shares a campaign with the brand
    final Campaign campaign = this.service.getById(campaignId);
    if (!campaign.getOwnerId().equals(requesterId)) {
      throw new BusinessRuleViolationException("Only the campaign owner can share it with a brand");
    }
    // The brand always becomes the parent of the agency once connected, regardless of which
    // side owned the invite that formed the connection.
    if (!this.connectionService.isParentOf(toUserId, requesterId)) {
      throw new BusinessRuleViolationException("Brand is not connected to this agency");
    }
    if (this.campaignShareService.existsByCampaignId(campaignId)) {
      throw new BusinessRuleViolationException("Campaign is already shared with a brand");
    }
    final CampaignShare saved =
        this.campaignShareService.create(
            CampaignShare.builder()
                .campaignId(campaignId)
                .toUserId(toUserId)
                .fromUserId(requesterId)
                .createdBy(requesterId)
                .updatedBy(requesterId)
                .build());
    return ShareCampaignResponseDto.builder()
        .campaignId(saved.getCampaignId())
        .toUserId(saved.getToUserId())
        .fromUserId(saved.getFromUserId())
        .createdAt(saved.getCreatedAt())
        .build();
  }

  public CampaignResponseDto getById(final UUID id) {
    final Campaign campaign = this.service.getById(id);
    if (campaign.getStatus() == CampaignStatus.CAMPAIGN_STATUS_DRAFT
        && campaign.getAssignmentsDraft() != null
        && !campaign.getAssignmentsDraft().isEmpty()) {
      final CampaignResponseDto draft =
          this.campaignMapper.toResponseFromDraft(campaign, campaign.getAssignmentsDraft());
      return enrichAssigneeNames(campaign, draft);
    }
    final List<CampaignAssignment> assignments =
        this.campaignAssignmentService.getByCampaignId(campaign.getId());
    return buildResponse(campaign, assignments);
  }

  @Transactional
  public CampaignResponseDto create(final UUID requesterId, final CampaignRequestDto request) {
    DateTimeUtils.validateEndDateNotInPast(request.getEndDate());
    validateCampaignSlots(request);
    final Product newProduct = this.productProcessor.saveProduct(request);
    final Campaign savedCampaign =
        this.service.create(
            this.campaignMapper.toCampaignEntity(request).toBuilder()
                .product(newProduct)
                .status(CampaignStatus.CAMPAIGN_STATUS_DRAFT)
                .createdBy(requesterId)
                .updatedBy(requesterId)
                .requiredSteps(normalizeRequiredSteps(request.getRequiredSteps()))
                .build());
    this.campaignEventPublisher.publishCampaignCreatedEvent(savedCampaign.getId(), requesterId);
    if (request.getAction() == CampaignAction.CAMPAIGN_ACTION_PUBLISH) {
      return publish(savedCampaign, requesterId, request.getAssignees());
    }
    return this.campaignMapper.toResponse(savedCampaign);
  }

  @Transactional
  public CampaignResponseDto updateCampaign(
      final UUID requesterId, final UUID id, final CampaignRequestDto request) {
    validateCampaignSlots(request);
    final Campaign existingCampaign = this.service.getById(id);
    if (existingCampaign.getStatus() != CampaignStatus.CAMPAIGN_STATUS_DRAFT) {
      throw new BusinessRuleViolationException(
          "Cannot update a campaign that is not in draft status");
    }

    final Product updatedProduct =
        this.productProcessor.updateProduct(existingCampaign.getProduct(), request);
    this.campaignMapper.updateCampaign(request, existingCampaign);

    final Campaign updatedCampaign =
        existingCampaign.toBuilder()
            .product(updatedProduct)
            .updatedBy(requesterId)
            .requiredSteps(normalizeRequiredSteps(request.getRequiredSteps()))
            .build();

    final Campaign savedCampaign = this.service.update(updatedCampaign);
    if (request.getAction() == CampaignAction.CAMPAIGN_ACTION_PUBLISH) {
      return publish(savedCampaign, requesterId, request.getAssignees());
    }
    return this.campaignMapper.toResponse(savedCampaign);
  }

  private CampaignResponseDto publish(
      final Campaign campaign,
      final UUID requesterId,
      final List<CampaignAssignmentRequestDto> assignees) {
    if (assignees == null) {
      throw new BusinessRuleViolationException("assignees cannot be null");
    }
    final List<CampaignAssignment> assignments =
        createSlotsAndAssignments(campaign, requesterId, assignees);
    final Campaign publishedCampaign =
        this.service.action(campaign.getId(), CampaignAction.CAMPAIGN_ACTION_PUBLISH, requesterId);
    return buildResponse(publishedCampaign, assignments);
  }

  private CampaignResponseDto buildResponse(
      final Campaign campaign, final List<CampaignAssignment> assignments) {
    return enrichAssigneeNames(campaign, this.campaignMapper.toResponse(campaign, assignments));
  }

  private CampaignResponseDto enrichAssigneeNames(
      final Campaign campaign, final CampaignResponseDto response) {
    if (response.getAssignments() == null || response.getAssignments().isEmpty()) {
      return response;
    }
    final Map<UUID, String> nameById =
        this.userService.getNamesByIds(
            response.getAssignments().stream()
                .map(CampaignAssignmentResponseDto::getAssigneeId)
                .toList());
    return response.toBuilder()
        .assignments(
            response.getAssignments().stream()
                .map(dto -> dto.toBuilder().assigneeName(nameById.get(dto.getAssigneeId())).build())
                .toList())
        .build();
  }

  private List<CampaignAssignment> createSlotsAndAssignments(
      final Campaign campaign,
      final UUID requesterId,
      final List<CampaignAssignmentRequestDto> assignees) {
    if (campaign.isOpenToAll()) {
      return createSlotsAndAssignmentsForOpenToAll(campaign, requesterId, assignees);
    }
    return createSlotsAndAssignmentsFromRequest(campaign, requesterId, assignees);
  }

  private List<CampaignAssignment> createSlotsAndAssignmentsForOpenToAll(
      final Campaign campaign,
      final UUID requesterId,
      final List<CampaignAssignmentRequestDto> assignees) {
    final CampaignSlot slot =
        this.campaignSlotService.create(
            CampaignSlot.builder()
                .campaignId(campaign.getId())
                .totalSlots(campaign.getTotalSlots())
                .slotsAvailable(campaign.getTotalSlots())
                .createdBy(requesterId)
                .updatedBy(requesterId)
                .isDeleted(false)
                .build());

    final List<CampaignAssignment> assignments = new ArrayList<>();
    for (final CampaignAssignmentRequestDto entry : assignees) {
      assignments.add(
          CampaignAssignment.builder()
              .campaignId(campaign.getId())
              .assignorId(entry.getAssignorId())
              .assigneeId(entry.getAssigneeId())
              .slotLimit(campaign.getTotalSlots())
              .adjustedCampaignPricePaise(entry.getAdjustedCampaignPricePaise())
              .commissionOfferedPaise(entry.getCommissionOfferedPaise())
              .campaignSlot(slot)
              .createdBy(requesterId)
              .updatedBy(requesterId)
              .isDeleted(false)
              .build());
    }
    return this.campaignAssignmentService.create(assignments);
  }

  private List<CampaignAssignment> createSlotsAndAssignmentsFromRequest(
      final Campaign campaign,
      final UUID requesterId,
      final List<CampaignAssignmentRequestDto> allAssignees) {
    final List<CampaignAssignmentRequestDto> assignees =
        allAssignees.stream()
            .filter(e -> e.getSlotOffered() != null && e.getSlotOffered() > 0L)
            .toList();
    if (assignees.isEmpty()) {
      throw new BusinessRuleViolationException(
          "No slots assigned to any assignee. Please check assignments and ensure slots are"
              + " assigned.");
    }
    final List<CampaignSlot> slots =
        assignees.stream()
            .map(
                entry ->
                    CampaignSlot.builder()
                        .campaignId(campaign.getId())
                        .totalSlots(entry.getSlotOffered().intValue())
                        .slotsAvailable(entry.getSlotOffered().intValue())
                        .createdBy(requesterId)
                        .updatedBy(requesterId)
                        .isDeleted(false)
                        .build())
            .toList();
    final List<CampaignSlot> savedSlots = this.campaignSlotService.create(slots);

    final List<CampaignAssignment> assignments = new ArrayList<>();
    for (int i = 0; i < assignees.size(); i++) {
      final CampaignAssignmentRequestDto entry = assignees.get(i);
      assignments.add(
          CampaignAssignment.builder()
              .campaignId(campaign.getId())
              .assignorId(entry.getAssignorId())
              .assigneeId(entry.getAssigneeId())
              .slotLimit(entry.getSlotOffered().intValue())
              .adjustedCampaignPricePaise(entry.getAdjustedCampaignPricePaise())
              .commissionOfferedPaise(entry.getCommissionOfferedPaise())
              .campaignSlot(savedSlots.get(i))
              .createdBy(requesterId)
              .updatedBy(requesterId)
              .isDeleted(false)
              .build());
    }
    return this.campaignAssignmentService.create(assignments);
  }

  /**
   * ORDER is always required — it's the claim-creation screenshot — regardless of what the request
   * selected, and CASHBACK is implicit (appended by {@code CampaignStepResolver}) so it is never
   * persisted as part of the selection.
   */
  private static List<CampaignStepType> normalizeRequiredSteps(
      final List<CampaignStepType> requiredSteps) {
    final Set<CampaignStepType> steps =
        requiredSteps == null ? new HashSet<>() : new HashSet<>(requiredSteps);
    steps.add(CampaignStepType.ORDER);
    steps.remove(CampaignStepType.CASHBACK);
    return steps.stream().sorted(Comparator.comparingInt(Enum::ordinal)).toList();
  }

  private void validateCampaignSlots(final CampaignRequestDto request) {
    if (!request.isOpenToAll() && request.getAssignees() != null) {
      final long totalAssignedSlots =
          request.getAssignees().stream()
              .mapToLong(
                  assignee -> assignee.getSlotOffered() != null ? assignee.getSlotOffered() : 0L)
              .sum();
      if (request.getTotalSlots() != null && totalAssignedSlots > request.getTotalSlots()) {
        throw new BusinessRuleViolationException(
            "Total assigned slots ("
                + totalAssignedSlots
                + ") cannot exceed campaign total slots ("
                + request.getTotalSlots()
                + ")");
      }
    }
  }
}
