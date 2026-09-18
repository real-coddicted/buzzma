package com.coddicted.buzzma.exchange.service;

import com.coddicted.buzzma.exchange.entity.AgencyExchangeProduct;
import java.util.List;
import java.util.UUID;

public interface ExchangeProductService {

  List<AgencyExchangeProduct> list(UUID agencyId);

  AgencyExchangeProduct create(AgencyExchangeProduct product, UUID requesterId);

  AgencyExchangeProduct update(AgencyExchangeProduct product, UUID requesterId);

  AgencyExchangeProduct delete(UUID id, UUID requesterId);
}
