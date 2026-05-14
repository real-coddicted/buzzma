package com.coddicted.buzzma.campaign.service.impl;

import com.coddicted.buzzma.campaign.entity.Commission;
import com.coddicted.buzzma.campaign.persistence.CommissionRepository;
import com.coddicted.buzzma.campaign.service.CommissionService;
import com.coddicted.buzzma.shared.common.BaseCrudService;
import com.coddicted.buzzma.shared.exception.NotFoundException;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CommissionServiceImpl extends BaseCrudService implements CommissionService {

  private static final Logger LOGGER = LoggerFactory.getLogger(CommissionServiceImpl.class);

  private final CommissionRepository commissionRepository;

  public CommissionServiceImpl(final CommissionRepository commissionRepository) {
    this.commissionRepository = commissionRepository;
  }

  @Override
  @Transactional(readOnly = true)
  public Commission getCommissionCharged(final UUID campaignId, final UUID chargedById) {
    return this.commissionRepository
        .findByCampaignIdAndChargedByIdAndIsDeletedFalse(campaignId, chargedById)
        .orElseThrow(
            () -> {
              LOGGER.warn(
                  "Commission not found for campaignId={} chargedById={}", campaignId, chargedById);
              return new NotFoundException("Commission not found for campaign: " + campaignId);
            });
  }

  @Override
  @Transactional
  public Commission create(final Commission commission, final UUID requesterId) {
    final Commission toSave =
        commission.toBuilder()
            .isDeleted(false)
            .createdBy(requesterId)
            .updatedBy(requesterId)
            .build();
    return this.commissionRepository.save(toSave);
  }

  @Override
  @Transactional
  public Commission update(final Commission commission, final UUID requesterId) {
    final Commission existing = mustFind(this.commissionRepository, commission.getId(), "Commission");
    final Commission updated =
        existing.toBuilder()
            .commissionPaise(commission.getCommissionPaise())
            .updatedBy(requesterId)
            .build();
    return this.commissionRepository.save(updated);
  }

  @Override
  @Transactional
  public Commission delete(final UUID commissionId, final UUID requesterId) {
    final Commission existing = mustFind(this.commissionRepository, commissionId, "Commission");
    final Commission deleted =
        existing.toBuilder().isDeleted(true).updatedBy(requesterId).build();
    return this.commissionRepository.save(deleted);
  }
}