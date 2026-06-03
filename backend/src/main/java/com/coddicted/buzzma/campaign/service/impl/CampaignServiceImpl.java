package com.coddicted.buzzma.campaign.service.impl;

import com.coddicted.buzzma.campaign.entity.Campaign;
import com.coddicted.buzzma.campaign.entity.CampaignAction;
import com.coddicted.buzzma.campaign.entity.CampaignAssignment;
import com.coddicted.buzzma.campaign.entity.CampaignAssignmentStatus;
import com.coddicted.buzzma.campaign.entity.CampaignSlot;
import com.coddicted.buzzma.campaign.entity.CampaignStatus;
import com.coddicted.buzzma.campaign.model.CampaignSummary;
import com.coddicted.buzzma.campaign.notification.CampaignEventPublisher;
import com.coddicted.buzzma.campaign.persistence.CampaignAssignmentRepository;
import com.coddicted.buzzma.campaign.persistence.CampaignRepository;
import com.coddicted.buzzma.campaign.persistence.CampaignSlotRepository;
import com.coddicted.buzzma.campaign.service.CampaignAssignmentService;
import com.coddicted.buzzma.campaign.service.CampaignService;
import com.coddicted.buzzma.campaign.service.CampaignStateMachine;
import com.coddicted.buzzma.shared.common.BaseCrudService;
import com.coddicted.buzzma.shared.constants.WellKnownSequences;
import com.coddicted.buzzma.shared.exception.BusinessRuleViolationException;
import com.coddicted.buzzma.shared.exception.ForbiddenException;
import com.coddicted.buzzma.shared.exception.NotFoundException;
import com.coddicted.buzzma.shared.service.CodeGenerationService;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CampaignServiceImpl extends BaseCrudService implements CampaignService {

  private final CampaignRepository campaignRepository;
  private final CampaignAssignmentRepository campaignAssignmentRepository;
  private final CampaignAssignmentService campaignAssignmentService;
  private final CampaignSlotRepository campaignSlotRepository;
  private final CampaignStateMachine stateMachine;
  private final CampaignEventPublisher campaignEventPublisher;
  private final CodeGenerationService codeGenerationService;

  public CampaignServiceImpl(
      final CampaignRepository campaignRepository,
      final CampaignAssignmentRepository campaignAssignmentRepository,
      final CampaignAssignmentService campaignAssignmentService,
      final CampaignSlotRepository campaignSlotRepository,
      final CampaignStateMachine stateMachine,
      final CampaignEventPublisher campaignEventPublisher,
      final CodeGenerationService codeGenerationService) {
    this.campaignRepository = campaignRepository;
    this.campaignAssignmentRepository = campaignAssignmentRepository;
    this.campaignAssignmentService = campaignAssignmentService;
    this.campaignSlotRepository = campaignSlotRepository;
    this.stateMachine = stateMachine;
    this.campaignEventPublisher = campaignEventPublisher;
    this.codeGenerationService = codeGenerationService;
  }

  @Override
  public Campaign getById(final UUID campaignId) {
    return this.campaignRepository
        .findById(campaignId)
        .orElseThrow(() -> new NotFoundException("Campaign not found: " + campaignId));
  }

  @Override
  @Transactional
  public Campaign create(final Campaign campaign) {
    return this.campaignRepository.save(
        campaign.toBuilder()
            .code(this.codeGenerationService.generateCodeFromSequence(WellKnownSequences.CAMPAIGN))
            .build());
  }

  @Override
  @Transactional
  public Campaign update(final Campaign campaign) {
    return this.campaignRepository.save(campaign);
  }

  @Override
  @Transactional
  public Campaign delete(final UUID campaignId, final UUID requesterId) {
    final Campaign existingCampaign = mustFind(this.campaignRepository, campaignId, "Campaign");
    final Campaign updatedCampaign =
        existingCampaign.toBuilder().isDeleted(true).updatedBy(requesterId).build();
    return this.campaignRepository.save(updatedCampaign);
  }

  @Override
  @Transactional
  public Campaign action(
      final UUID campaignId, final CampaignAction campaignAction, final UUID requesterId) {
    final CampaignStatus target =
        switch (campaignAction) {
          case CAMPAIGN_ACTION_PUBLISH, CAMPAIGN_ACTION_RESUME ->
              CampaignStatus.CAMPAIGN_STATUS_ACTIVE;
          case CAMPAIGN_ACTION_PAUSE -> CampaignStatus.CAMPAIGN_STATUS_PAUSED;
          case CAMPAIGN_ACTION_CLOSE -> CampaignStatus.CAMPAIGN_STATUS_CLOSED;
          case CAMPAIGN_ACTION_COMPLETE -> CampaignStatus.CAMPAIGN_STATUS_COMPLETED;
        };
    return transitionTo(campaignId, target, requesterId);
  }

  @Override
  @Transactional(readOnly = true)
  public Set<Campaign> findCampaignsById(final Set<UUID> campaignIdSet) {
    return this.campaignRepository.findByIdInAndIsDeletedFalse(campaignIdSet);
  }

  @Override
  @Transactional
  public Campaign copy(final UUID campaignId, final UUID requesterId) {
    final Campaign src = mustFind(this.campaignRepository, campaignId, "Campaign");
    final Campaign copy =
        src.toBuilder()
            .id(null)
            .code(this.codeGenerationService.generateCodeFromSequence(WellKnownSequences.CAMPAIGN))
            .title(src.getTitle() + " (Copy)")
            .status(CampaignStatus.CAMPAIGN_STATUS_DRAFT)
            .createdAt(null)
            .updatedAt(null)
            .createdBy(requesterId)
            .updatedBy(requesterId)
            .build();
    final Campaign saved = this.campaignRepository.save(copy);
    // Todo: Copy Product
    final List<UUID> srcAssignmentIds =
        this.campaignAssignmentRepository.findByCampaignId(campaignId).stream()
            .map(CampaignAssignment::getId)
            .toList();
    if (!srcAssignmentIds.isEmpty()) {
      this.campaignAssignmentService.copy(srcAssignmentIds, saved.getId(), requesterId);
    }
    this.campaignEventPublisher.publishCampaignCreatedEvent(saved.getId(), requesterId);
    return saved;
  }

  @Override
  @Transactional(readOnly = true)
  public List<CampaignSummary> getByOwnerId(final UUID ownerId) {
    final List<Campaign> campaigns =
        this.campaignRepository.findByOwnerIdAndIsDeletedFalse(ownerId);
    final List<UUID> campaignIds = campaigns.stream().map(Campaign::getId).toList();
    final Map<UUID, CampaignSlot> slotsByCampaignId =
        this.campaignSlotRepository.findByCampaignIdInAndIsDeletedFalse(campaignIds).stream()
            .collect(
                Collectors.toMap(
                    CampaignSlot::getCampaignId,
                    Function.identity(),
                    (a, b) ->
                        a.toBuilder()
                            .totalSlots(a.getTotalSlots() + b.getTotalSlots())
                            .slotsAvailable(a.getSlotsAvailable() + b.getSlotsAvailable())
                            .build()));
    return campaigns.stream()
        .map(
            campaign -> {
              final CampaignSlot slot = slotsByCampaignId.get(campaign.getId());
              final int slotsClaimed =
                  slot != null ? slot.getTotalSlots() - slot.getSlotsAvailable() : 0;
              return CampaignSummary.builder()
                  .campaign(campaign)
                  .slotsClaimed(slotsClaimed)
                  .build();
            })
        .toList();
  }

  private Campaign transitionTo(
      final UUID campaignId, final CampaignStatus target, final UUID requesterId) {
    final Campaign campaign = getById(campaignId);
    final boolean isPublish =
        campaign.getStatus() == CampaignStatus.CAMPAIGN_STATUS_DRAFT
            && target == CampaignStatus.CAMPAIGN_STATUS_ACTIVE;
    if (isPublish) {
      if (!campaign.getOwnerId().equals(requesterId)) {
        throw new ForbiddenException("Only the campaign owner can publish this campaign");
      }
      final List<CampaignAssignment> assignments =
          this.campaignAssignmentRepository.findByCampaignId(campaignId);
      if (assignments.isEmpty()) {
        throw new BusinessRuleViolationException(
            "Campaign must have at least one assignment before publishing");
      }
      this.stateMachine.transition(campaign, target);
      assignments.forEach(
          a -> a.setStatus(CampaignAssignmentStatus.CAMPAIGN_ASSIGNMENT_STATUS_LOCKED));
      this.campaignAssignmentRepository.saveAll(assignments);
    } else {
      this.stateMachine.transition(campaign, target);
    }
    return this.campaignRepository.save(campaign);
  }
}
