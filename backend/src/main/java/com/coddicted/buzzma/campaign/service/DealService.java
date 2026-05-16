package com.coddicted.buzzma.campaign.service;

import com.coddicted.buzzma.campaign.entity.Deal;
import org.springframework.data.domain.Page;

import java.util.UUID;

public interface DealService {

    Deal getById(UUID id);

    Deal create(Deal deal);

    Page<Deal> getUnclaimedDeals(UUID ownerId, UUID requesterId, int page, int size);
}
