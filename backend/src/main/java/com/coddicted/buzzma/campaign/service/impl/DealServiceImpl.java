package com.coddicted.buzzma.campaign.service.impl;

import com.coddicted.buzzma.campaign.entity.Deal;
import com.coddicted.buzzma.campaign.persistence.DealRepository;
import com.coddicted.buzzma.campaign.service.DealService;
import com.coddicted.buzzma.shared.common.BaseCrudService;
import com.coddicted.buzzma.shared.constants.WellKnownSequences;
import com.coddicted.buzzma.shared.service.CodeGenerationService;
import java.util.Collection;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DealServiceImpl extends BaseCrudService implements DealService {

  private static final Logger LOGGER = LoggerFactory.getLogger(DealServiceImpl.class);

  private final DealRepository dealRepository;
  private final CodeGenerationService codeGenerationService;

  public DealServiceImpl(
      final DealRepository dealRepository, final CodeGenerationService codeGenerationService) {
    this.dealRepository = dealRepository;
    this.codeGenerationService = codeGenerationService;
  }

  @Override
  public Deal getById(final UUID id) {
    return mustFind(this.dealRepository, id, "Deal");
  }

  @Override
  public Deal create(final Deal deal) {
    final String code =
        this.codeGenerationService.generateCodeFromSequence(WellKnownSequences.DEAL);
    final Deal toSave = deal.toBuilder().code(code).build();
    return this.dealRepository.save(toSave);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<Deal> getActiveDeals(
      final Collection<UUID> ownerIds, final UUID requesterId, final int page, final int size) {
    LOGGER.info("Get active deals for user {}", requesterId);
    return this.dealRepository.findActiveDeals(ownerIds, PageRequest.of(page, size));
  }
}
