package com.coddicted.buzzma.campaign.service.impl;

import com.coddicted.buzzma.campaign.entity.CampaignAssignment;
import com.coddicted.buzzma.campaign.entity.CampaignAssignmentStatus;
import com.coddicted.buzzma.campaign.persistence.CampaignAssignmentRepository;
import com.coddicted.buzzma.campaign.service.CampaignAssignmentService;
import com.coddicted.buzzma.shared.common.BaseCrudService;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class CampaignAssignmentServiceImpl extends BaseCrudService implements CampaignAssignmentService {

    private final CampaignAssignmentRepository campaignAssignmentRepository;

    public CampaignAssignmentServiceImpl(final CampaignAssignmentRepository campaignAssignmentRepository) {
        this.campaignAssignmentRepository = campaignAssignmentRepository;
    }

    @Override
    public List<CampaignAssignment> listAssignmentsByAssignor(final UUID assignorId) {
        return this.campaignAssignmentRepository.findByAssignorId(assignorId);
    }

    @Override
    public List<CampaignAssignment> listAssignmentsByAssignee(final UUID assigneeId) {
        return this.campaignAssignmentRepository.findByAssigneeId(assigneeId);
    }

    @Override
    public List<CampaignAssignment> create(final List<CampaignAssignment> assignments) {
        return this.campaignAssignmentRepository.saveAll(assignments);
    }

    @Override
    public List<CampaignAssignment> update(final List<CampaignAssignment> assignments) {
        return this.campaignAssignmentRepository.saveAll(assignments);
    }

    @Override
    public CampaignAssignment delete(final UUID campaignAssignmentId, final UUID requesterId) {
        final CampaignAssignment existingCampaignAssignment = mustFind(campaignAssignmentRepository, campaignAssignmentId, "Campaign Assignment");
        final CampaignAssignment updatedCampaignAssignment = existingCampaignAssignment.toBuilder().isDeleted(true).updatedBy(requesterId).updatedAt(Instant.now()).build();
        return this.campaignAssignmentRepository.save(updatedCampaignAssignment);
    }

    @Override
    public List<CampaignAssignment> copy(final List<UUID> srcCampaignAssignments, final UUID destCampaignId, final UUID requesterId) {
        final List<CampaignAssignment> copies = campaignAssignmentRepository.findAllById(srcCampaignAssignments)
                .stream()
                .map(src -> src.toBuilder()
                        .id(null)
                        .campaignId(destCampaignId)
                        .status(CampaignAssignmentStatus.CAMPAIGN_ASSIGNMENT_STATUS_DRAFT)
                        .createdAt(null)
                        .createdBy(requesterId)
                        .updatedAt(null)
                        .updatedBy(requesterId)
                        .build())
                .toList();
        return campaignAssignmentRepository.saveAll(copies);
    }

    @Override
    public List<CampaignAssignment> lockAssignments(final List<UUID> campaignAssignments) {
        return List.of();
    }
}
