package com.coddicted.buzzma.campaign.processor;

import com.coddicted.buzzma.campaign.dto.CampaignAssignmentRequestDto;
import com.coddicted.buzzma.campaign.dto.CampaignRequestDto;
import com.coddicted.buzzma.campaign.dto.CampaignResponseDto;
import com.coddicted.buzzma.campaign.entity.Campaign;
import com.coddicted.buzzma.campaign.entity.CampaignAssignment;
import com.coddicted.buzzma.campaign.entity.CampaignSlot;
import com.coddicted.buzzma.campaign.entity.CampaignStatus;
import com.coddicted.buzzma.campaign.entity.Product;
import com.coddicted.buzzma.campaign.mapper.CampaignAssignmentMapper;
import com.coddicted.buzzma.campaign.mapper.CampaignMapper;
import com.coddicted.buzzma.campaign.persistence.CampaignAssignmentRepository;
import com.coddicted.buzzma.campaign.persistence.CampaignSlotRepository;
import com.coddicted.buzzma.campaign.service.CampaignAssignmentService;
import com.coddicted.buzzma.campaign.service.CampaignService;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class CampaignProcessor {

  private final CampaignService service;
  private final CampaignMapper campaignMapper;
  private final ProductProcessor productProcessor;
  private final CampaignAssignmentRepository campaignAssignmentRepository;
  private final CampaignAssignmentMapper campaignAssignmentMapper;
  private final CampaignAssignmentService campaignAssignmentService;
  private final CampaignSlotRepository campaignSlotRepository;

  public CampaignProcessor(
      final CampaignService service,
      final CampaignMapper campaignMapper,
      final ProductProcessor productProcessor,
      final CampaignAssignmentRepository campaignAssignmentRepository,
      final CampaignAssignmentMapper campaignAssignmentMapper,
      final CampaignAssignmentService campaignAssignmentService,
      final CampaignSlotRepository campaignSlotRepository) {
    this.service = service;
    this.campaignMapper = campaignMapper;
    this.productProcessor = productProcessor;
    this.campaignAssignmentRepository = campaignAssignmentRepository;
    this.campaignAssignmentMapper = campaignAssignmentMapper;
    this.campaignAssignmentService = campaignAssignmentService;
    this.campaignSlotRepository = campaignSlotRepository;
  }

  public CampaignResponseDto getById(final UUID id) {
    final Campaign campaign = this.service.getById(id);
    final List<CampaignAssignment> assignments =
        this.campaignAssignmentRepository.findByCampaignIdAndIsDeletedFalse(campaign.getId());
    return this.campaignMapper.toResponse(campaign, assignments);
  }

  @Transactional
  public CampaignResponseDto create(final UUID requesterId, final CampaignRequestDto request) {
    final Product newProduct = this.productProcessor.saveProduct(request);
    final Campaign savedCampaign =
        this.service.create(
            this.campaignMapper.toCampaignEntity(request).toBuilder()
                .product(newProduct)
                .status(CampaignStatus.CAMPAIGN_STATUS_DRAFT)
                .createdBy(requesterId)
                .updatedBy(requesterId)
                .build());

    final CampaignSlot slot = new CampaignSlot();
    slot.setCampaignId(savedCampaign.getId());
    slot.setTotalSlots(savedCampaign.getTotalSlots());
    slot.setSlotsAvailable(savedCampaign.getTotalSlots());
    slot.setCreatedBy(requesterId);
    slot.setUpdatedBy(requesterId);
    slot.setDeleted(false);
    final CampaignSlot savedSlot = this.campaignSlotRepository.save(slot);

    final List<CampaignAssignment> savedAssignments;
    if (request.getAssignees() != null && !request.getAssignees().isEmpty()) {
      final List<CampaignAssignment> assignments =
          this.campaignAssignmentMapper.toCampaignAssignments(request.getAssignees()).stream()
              .map(
                  a ->
                      a.toBuilder()
                          .id(null)
                          .campaignId(savedCampaign.getId())
                          .campaignSlot(savedSlot)
                          .status(null)
                          .createdBy(requesterId)
                          .updatedBy(requesterId)
                          .isDeleted(false)
                          .build())
              .toList();
      savedAssignments = this.campaignAssignmentService.create(assignments);
    } else {
      savedAssignments = List.of();
    }

    return this.campaignMapper.toResponse(savedCampaign, savedAssignments);
  }

  @Transactional
  public CampaignResponseDto updateCampaign(
      final UUID requesterId, final UUID id, final CampaignRequestDto request) {
    final Campaign existingCampaign = this.service.getById(id);
    final int oldTotalSlots = existingCampaign.getTotalSlots();

    final Product updatedProduct = this.productProcessor.saveProduct(request);
    this.campaignMapper.updateCampaign(request, existingCampaign);

    final Campaign updatedCampaign =
        existingCampaign.toBuilder().product(updatedProduct).updatedBy(requesterId).build();

    final Campaign savedCampaign = this.service.update(updatedCampaign);

    final CampaignSlot savedSlot =
        updateOrCreateSlot(id, savedCampaign, oldTotalSlots, requesterId);

    final List<CampaignAssignment> savedAssignments =
        syncAssignments(id, request, savedSlot, requesterId);

    return this.campaignMapper.toResponse(savedCampaign, savedAssignments);
  }

  private CampaignSlot updateOrCreateSlot(
      final UUID campaignId,
      final Campaign savedCampaign,
      final int oldTotalSlots,
      final UUID requesterId) {
    final Optional<CampaignSlot> existing =
        this.campaignSlotRepository.findByCampaignIdAndIsDeletedFalse(campaignId);
    // Todo: fluent
    if (existing.isPresent()) {
      final CampaignSlot slot = existing.get();
      final int delta = savedCampaign.getTotalSlots() - oldTotalSlots;
      slot.setTotalSlots(savedCampaign.getTotalSlots());
      slot.setSlotsAvailable(Math.max(0, slot.getSlotsAvailable() + delta));
      slot.setUpdatedBy(requesterId);
      return this.campaignSlotRepository.save(slot);
    }
    final CampaignSlot slot =
        CampaignSlot.builder()
            .campaignId(savedCampaign.getId())
            .totalSlots(savedCampaign.getTotalSlots())
            .slotsAvailable(savedCampaign.getTotalSlots())
            .createdBy(requesterId)
            .updatedBy(requesterId)
            .isDeleted(false)
            .build();
    return this.campaignSlotRepository.save(slot);
  }

  private List<CampaignAssignment> syncAssignments(
      final UUID campaignId,
      final CampaignRequestDto request,
      final CampaignSlot slot,
      final UUID requesterId) {
    final List<CampaignAssignment> existing =
        this.campaignAssignmentRepository.findByCampaignIdAndIsDeletedFalse(campaignId);
    final Map<UUID, CampaignAssignment> existingByAssigneeId =
        existing.stream()
            .collect(Collectors.toMap(CampaignAssignment::getAssigneeId, Function.identity()));

    final List<CampaignAssignmentRequestDto> requestedAssignees =
        request.getAssignees() != null ? request.getAssignees() : List.of();

    final List<UUID> requestedAssigneeIds =
        requestedAssignees.stream().map(CampaignAssignmentRequestDto::getAssigneeId).toList();

    // soft-delete assignments removed from the request
    final List<CampaignAssignment> toDelete =
        existing.stream()
            .filter(a -> !requestedAssigneeIds.contains(a.getAssigneeId()))
            .map(a -> a.toBuilder().isDeleted(true).updatedBy(requesterId).build())
            .toList();
    if (!toDelete.isEmpty()) {
      this.campaignAssignmentService.update(toDelete);
    }

    // update existing or create new
    final List<CampaignAssignment> toSave = new ArrayList<>();
    for (final CampaignAssignmentRequestDto dto : requestedAssignees) {
      final CampaignAssignment existingCampaignAssignment =
          existingByAssigneeId.get(dto.getAssigneeId());
      if (existingCampaignAssignment != null) {
        toSave.add(
            existingCampaignAssignment.toBuilder()
                .commissionOfferedPaise(dto.getCommissionOfferedPaise())
                .slotLimit(dto.getSlotOffered().intValue())
                .adjustedCampaignPricePaise(dto.getAdjustedCampaignPricePaise())
                .updatedBy(requesterId)
                .build());
      } else {
        toSave.add(
            this.campaignAssignmentMapper.toCampaignAssignment(dto).toBuilder()
                .id(null)
                .campaignId(campaignId)
                .campaignSlot(slot)
                .status(null)
                .createdBy(requesterId)
                .updatedBy(requesterId)
                .isDeleted(false)
                .build());
      }
    }
    return this.campaignAssignmentService.update(toSave);
  }
}
