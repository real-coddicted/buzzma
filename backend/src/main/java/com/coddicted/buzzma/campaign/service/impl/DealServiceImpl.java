package com.coddicted.buzzma.campaign.service.impl;

import com.coddicted.buzzma.campaign.entity.Deal;
import com.coddicted.buzzma.campaign.persistence.DealRepository;
import com.coddicted.buzzma.campaign.service.DealService;
import com.coddicted.buzzma.shared.common.BaseCrudService;
import org.springframework.stereotype.Service;

@Service
public class DealServiceImpl extends BaseCrudService implements DealService {

    private final DealRepository dealRepository;

    public DealServiceImpl(final DealRepository dealRepository) {
        this.dealRepository = dealRepository;
    }

    @Override
    public Deal create(final Deal deal) {
        return this.dealRepository.save(deal);
    }
}
