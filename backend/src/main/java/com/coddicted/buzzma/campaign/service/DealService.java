package com.coddicted.buzzma.campaign.service;

import com.coddicted.buzzma.campaign.entity.Deal;
import org.springframework.data.domain.Page;

import java.util.UUID;

public interface DealService {

    Page<Deal> getDeals(
            final int page, final int size, final UUID ownerId);

    Deal getById(UUID dealId);
}
