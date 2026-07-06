package com.coddicted.buzzma.campaign.service;

import com.coddicted.buzzma.campaign.entity.Deal;
import java.util.UUID;
import org.springframework.data.domain.Page;

public interface DealService {

  Deal getById(UUID id);

  Deal create(Deal deal);

  Page<Deal> getActiveDeals(UUID ownerId, UUID requesterId, int page, int size);
}
