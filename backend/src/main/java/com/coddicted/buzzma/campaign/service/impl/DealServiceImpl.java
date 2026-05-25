package com.coddicted.buzzma.campaign.service.impl;

import com.coddicted.buzzma.campaign.entity.Deal;
import com.coddicted.buzzma.campaign.persistence.DealRepository;
import com.coddicted.buzzma.campaign.service.DealService;
import com.coddicted.buzzma.shared.common.BaseCrudService;
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

  public DealServiceImpl(final DealRepository dealRepository) {
    this.dealRepository = dealRepository;
  }

  @Override
  public Deal getById(final UUID id) {
    return mustFind(this.dealRepository, id, "Deal");
  }

  @Override
  public Deal create(final Deal deal) {
    return this.dealRepository.save(deal);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<Deal> getUnclaimedDeals(
      final UUID ownerId, final UUID requesterId, final int page, final int size) {
    LOGGER.info("Get unclaimed deals for user {}", requesterId);
    return this.dealRepository.findUnclaimedDeals(ownerId, requesterId, PageRequest.of(page, size));
  }
}
