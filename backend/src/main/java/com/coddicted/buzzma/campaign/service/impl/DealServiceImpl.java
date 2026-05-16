package com.coddicted.buzzma.campaign.service.impl;

import com.coddicted.buzzma.campaign.entity.Deal;
import com.coddicted.buzzma.campaign.persistence.DealRepository;
import com.coddicted.buzzma.campaign.service.DealService;
import com.coddicted.buzzma.shared.common.BaseCrudService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class DealServiceImpl extends BaseCrudService implements DealService {

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
        return this.dealRepository.findUnclaimedDeals(ownerId, requesterId, PageRequest.of(page, size));
    }

}
