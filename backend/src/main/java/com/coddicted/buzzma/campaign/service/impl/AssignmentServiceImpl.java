package com.coddicted.buzzma.campaign.service.impl;

import com.coddicted.buzzma.campaign.dto.AssignmentSummaryResponseDto;
import com.coddicted.buzzma.campaign.entity.Campaign;
import com.coddicted.buzzma.campaign.entity.CampaignAssignment;
import com.coddicted.buzzma.campaign.entity.CampaignAssignmentStatus;
import com.coddicted.buzzma.campaign.entity.CampaignSlot;
import com.coddicted.buzzma.campaign.entity.Commission;
import com.coddicted.buzzma.campaign.entity.Deal;
import com.coddicted.buzzma.campaign.mapper.AssignmentMapper;
import com.coddicted.buzzma.campaign.model.Assignment;
import com.coddicted.buzzma.campaign.service.AssignmentService;
import com.coddicted.buzzma.campaign.service.CampaignAssignmentService;
import com.coddicted.buzzma.campaign.service.CampaignService;
import com.coddicted.buzzma.campaign.service.CommissionService;
import com.coddicted.buzzma.campaign.service.DealService;
import com.coddicted.buzzma.shared.common.BaseCrudService;
import com.coddicted.buzzma.shared.exception.BusinessRuleViolationException;
import com.coddicted.buzzma.shared.exception.ForbiddenException;
import jakarta.validation.constraints.NotNull;
import java.math.BigInteger;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class AssignmentServiceImpl extends BaseCrudService implements AssignmentService {

  private static final Logger LOGGER = LoggerFactory.getLogger(AssignmentServiceImpl.class);

  private final CampaignService campaignService;
  private final CampaignAssignmentService campaignAssignmentService;
  private final CommissionService commissionService;
  private final DealService dealService;
  private final AssignmentMapper assignmentMapper;

  public AssignmentServiceImpl(
      final CampaignService campaignService,
      final CampaignAssignmentService campaignAssignmentService,
      final CommissionService commissionService,
      final DealService dealService,
      final AssignmentMapper assignmentMapper) {
    this.campaignService = campaignService;
    this.campaignAssignmentService = campaignAssignmentService;
    this.commissionService = commissionService;
    this.dealService = dealService;
    this.assignmentMapper = assignmentMapper;
  }

  @Override
  @Transactional(readOnly = true)
  public Assignment getAssignmentById(final UUID id, final UUID requesterId) {
    final CampaignAssignment ca = this.campaignAssignmentService.getById(id);
    if (!requesterId.equals(ca.getAssigneeId())) {
      throw new ForbiddenException("Assignment does not belong to the requesting user.");
    }
    final Campaign campaign = this.campaignService.getById(ca.getCampaignId());
    return Assignment.builder()
        .campaign(campaign)
        .campaignAssignment(ca)
        .campaignSlot(ca.getCampaignSlot())
        .build();
  }

  @Override
  @Transactional(readOnly = true)
  public Set<Assignment> getAssignments(
      final UUID assigneeId, final CampaignAssignmentStatus status) {
    final List<CampaignAssignment> campaignAssignmentList =
        this.campaignAssignmentService.listAssignmentsByAssignee(assigneeId, status);
    final Set<UUID> campaignIdSet =
        campaignAssignmentList.stream()
            .map(CampaignAssignment::getCampaignId)
            .collect(Collectors.toSet());
    final Set<Campaign> campaignSet = this.campaignService.findCampaignsById(campaignIdSet);
    final Set<CampaignAssignment> campaignAssignmentSet = new HashSet<>(campaignAssignmentList);
    return toAssignmentSet(campaignSet, campaignAssignmentSet);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<AssignmentSummaryResponseDto> getAssignmentSummaries(
      final UUID assigneeId, final CampaignAssignmentStatus status, final Pageable pageable) {
    return this.campaignAssignmentService
        .listAssignmentSummaries(assigneeId, status, pageable)
        .map(this.assignmentMapper::toSummaryResponse);
  }

  @Override
  @Transactional
  public boolean publishAssignment(
      final UUID campaignId,
      final UUID campaignAssignmentId,
      @NotNull final BigInteger commissionCharged,
      @NotNull final BigInteger dealPrice,
      final UUID chargedById,
      final String affiliateUrl) {
    final Campaign campaign = this.campaignService.getById(campaignId);
    if (StringUtils.hasText(affiliateUrl) && !campaign.isAffiliateLinkAllowed()) {
      throw new BusinessRuleViolationException(
          "Affiliate URL is not allowed for this campaign. This may be fraudulent activity.");
    }
    final CampaignAssignment campaignAssignment =
        this.campaignAssignmentService.getById(campaignAssignmentId);
    final Commission commission =
        Commission.builder()
            .commissionPaise(commissionCharged)
            .campaignId(campaignId)
            .chargedById(chargedById)
            .createdBy(chargedById)
            .updatedBy(chargedById)
            .build();
    this.commissionService.create(commission, chargedById);
    // create deal entity
    final Deal deal =
        toDeal(
            campaign, campaignAssignment.getCampaignSlot(), dealPrice, chargedById, affiliateUrl);
    this.dealService.create(deal);
    final CampaignAssignment updated =
        campaignAssignment.toBuilder()
            .status(CampaignAssignmentStatus.CAMPAIGN_ASSIGNMENT_STATUS_PUBLISHED)
            .updatedBy(chargedById)
            .build();
    this.campaignAssignmentService.update(updated);
    return true;
    // Todo: Add error handling
  }

  // Todo: optimize
  private Set<Assignment> toAssignmentSet(
      final Set<Campaign> campaignSet, final Set<CampaignAssignment> campaignAssignmentSet) {
    final Map<UUID, Campaign> campaignById =
        campaignSet.stream().collect(Collectors.toMap(Campaign::getId, c -> c));
    return campaignAssignmentSet.stream()
        .map(
            ca ->
                Assignment.builder()
                    .campaign(campaignById.get(ca.getCampaignId()))
                    .campaignAssignment(ca)
                    .campaignSlot(ca.getCampaignSlot())
                    .build())
        .collect(Collectors.toSet());
  }

  private Deal toDeal(
      final Campaign campaign,
      final CampaignSlot campaignSlot,
      final BigInteger dealPricePaise,
      final UUID ownerId,
      final String affiliateUrl) {
    return Deal.builder()
        .campaign(campaign)
        .campaignSlot(campaignSlot)
        .dealPricePaise(dealPricePaise)
        .ownerId(ownerId)
        .createdBy(ownerId)
        .updatedBy(ownerId)
        .affiliateUrl(affiliateUrl)
        .build();
  }
}
