package com.coddicted.buzzma.campaign.service.impl;

import com.coddicted.buzzma.campaign.entity.Campaign;
import com.coddicted.buzzma.campaign.entity.CampaignAction;
import com.coddicted.buzzma.campaign.entity.CampaignAssignment;
import com.coddicted.buzzma.campaign.entity.CampaignAssignmentStatus;
import com.coddicted.buzzma.campaign.entity.CampaignStatus;
import com.coddicted.buzzma.campaign.persistence.CampaignAssignmentRepository;
import com.coddicted.buzzma.campaign.persistence.CampaignRepository;
import com.coddicted.buzzma.campaign.service.CampaignAssignmentService;
import com.coddicted.buzzma.campaign.service.CampaignService;
import com.coddicted.buzzma.campaign.service.CampaignStateMachine;
import com.coddicted.buzzma.shared.common.BaseCrudService;
import com.coddicted.buzzma.shared.exception.BusinessRuleViolationException;
import com.coddicted.buzzma.shared.exception.ForbiddenException;
import com.coddicted.buzzma.shared.exception.NotFoundException;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CampaignServiceImpl extends BaseCrudService implements CampaignService {

  private final CampaignRepository campaignRepository;
  private final CampaignAssignmentRepository campaignAssignmentRepository;
  private final CampaignAssignmentService campaignAssignmentService;
  private final CampaignStateMachine stateMachine;

  public CampaignServiceImpl(
      final CampaignRepository campaignRepository,
      final CampaignAssignmentRepository campaignAssignmentRepository,
      final CampaignAssignmentService campaignAssignmentService,
      final CampaignStateMachine stateMachine) {
    this.campaignRepository = campaignRepository;
    this.campaignAssignmentRepository = campaignAssignmentRepository;
    this.campaignAssignmentService = campaignAssignmentService;
    this.stateMachine = stateMachine;
  }

  @Override
  public Campaign getById(final UUID campaignId) {
    return campaignRepository
        .findById(campaignId)
        .orElseThrow(() -> new NotFoundException("Campaign not found: " + campaignId));
  }

  @Override
  @Transactional
  public Campaign create(final Campaign campaign) {
    return campaignRepository.save(campaign);
  }

  @Override
  @Transactional
  public Campaign update(final Campaign campaign) {
    return campaignRepository.save(campaign);
  }

  @Override
  @Transactional
  public Campaign delete(final UUID campaignId, final UUID requesterId) {
    final Campaign existingCampaign = mustFind(this.campaignRepository, campaignId, "Campaign");
    final Campaign updatedCampaign =
        existingCampaign.toBuilder().isDeleted(true).updatedBy(requesterId).build();
    return campaignRepository.save(updatedCampaign);
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
  @Transactional
  public Campaign copy(final UUID campaignId, final UUID requesterId) {
    final Campaign src = mustFind(campaignRepository, campaignId, "Campaign");
    final Campaign copy =
        src.toBuilder()
            .id(null)
            .status(CampaignStatus.CAMPAIGN_STATUS_DRAFT)
            .createdAt(null)
            .updatedAt(null)
            .createdBy(requesterId)
            .updatedBy(requesterId)
            .build();
    final Campaign saved = campaignRepository.save(copy);

    final List<UUID> srcAssignmentIds =
        campaignAssignmentRepository.findByCampaignId(campaignId).stream()
            .map(CampaignAssignment::getId)
            .toList();
    if (!srcAssignmentIds.isEmpty()) {
      campaignAssignmentService.copy(srcAssignmentIds, saved.getId(), requesterId);
    }

    return saved;
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
          campaignAssignmentRepository.findByCampaignId(campaignId);
      if (assignments.isEmpty()) {
        throw new BusinessRuleViolationException(
            "Campaign must have at least one assignment before publishing");
      }
      stateMachine.transition(campaign, target);
      assignments.forEach(
          a -> a.setStatus(CampaignAssignmentStatus.CAMPAIGN_ASSIGNMENT_STATUS_LOCKED));
      campaignAssignmentRepository.saveAll(assignments);
    } else {
      stateMachine.transition(campaign, target);
    }
    return campaignRepository.save(campaign);
  }
}
