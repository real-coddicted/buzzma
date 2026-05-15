package com.coddicted.buzzma.campaign.service.impl;

import com.coddicted.buzzma.campaign.entity.*;
import com.coddicted.buzzma.campaign.model.Assignment;
import com.coddicted.buzzma.campaign.service.*;
import com.coddicted.buzzma.shared.common.BaseCrudService;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AssignmentServiceImpl extends BaseCrudService implements AssignmentService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AssignmentServiceImpl.class);

    private final CampaignService campaignService;
    private final CampaignAssignmentService campaignAssignmentService;
    private final CommissionService commissionService;
    private final DealService dealService;

    public AssignmentServiceImpl(final CampaignService campaignService, final CampaignAssignmentService campaignAssignmentService,
                                 final CommissionService commissionService,
                                 final DealService dealService) {
        this.campaignService = campaignService;
        this.campaignAssignmentService = campaignAssignmentService;
        this.commissionService = commissionService;
        this.dealService = dealService;
    }

    @Override
    @Transactional(readOnly = true)
    public Set<Assignment> getAssignments(final UUID assigneeId, final CampaignAssignmentStatus status) {
        final List<CampaignAssignment> campaignAssignmentList =
                this.campaignAssignmentService.listAssignmentsByAssignee(assigneeId, status);
        final Set<UUID> campaignIdSet = campaignAssignmentList.stream().map(CampaignAssignment::getCampaignId).collect(Collectors.toSet());
        final Set<Campaign> campaignSet = this.campaignService.findCampaignsById(campaignIdSet);
        final Set<CampaignAssignment> campaignAssignmentSet = new HashSet<>(campaignAssignmentList);
        return toAssignmentSet(campaignSet, campaignAssignmentSet);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean publishAssignment(final UUID campaignId, final UUID campaignAssignmentId,
                                  @NotNull final BigInteger commissionCharged, @NotNull final BigInteger dealPrice,
                                  final UUID chargedById) {
        final Campaign campaign = this.campaignService.getById(campaignId);
        final CampaignAssignment campaignAssignment = this.campaignAssignmentService.getById(campaignAssignmentId);
        final Commission commission = Commission.builder().commissionPaise(commissionCharged).chargedById(chargedById).createdBy(chargedById).updatedBy(chargedById).build();
        this.commissionService.create(commission, chargedById);
        // create deal entity
        final Deal deal = toDeal(campaign, campaignAssignment.getCampaignSlot(), dealPrice, chargedById);
        this.dealService.create(deal);
        return true;
        // Todo: Add error handling
    }

    //Todo: optimize
    private Set<Assignment> toAssignmentSet(final Set<Campaign> campaignSet, final Set<CampaignAssignment> campaignAssignmentSet) {
        final Map<UUID, Campaign> campaignById =
                campaignSet.stream().collect(Collectors.toMap(Campaign::getId, c -> c));
        return campaignAssignmentSet.stream()
                .map(ca -> Assignment.builder()
                        .campaign(campaignById.get(ca.getCampaignId()))
                        .campaignAssignment(ca)
                        .campaignSlot(ca.getCampaignSlot())
                        .build())
                .collect(Collectors.toSet());
    }

    private Deal toDeal(final Campaign campaign, final CampaignSlot campaignSlot, final BigInteger dealPricePaise, final UUID ownerId) {
        return Deal.builder()
                .campaign(campaign)
                .campaignSlot(campaignSlot)
                .dealPricePaise(dealPricePaise)
                .ownerId(ownerId)
                .createdBy(ownerId)
                .updatedBy(ownerId)
                .build();
    }
}
