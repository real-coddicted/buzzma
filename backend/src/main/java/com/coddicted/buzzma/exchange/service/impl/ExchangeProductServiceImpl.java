package com.coddicted.buzzma.exchange.service.impl;

import com.coddicted.buzzma.exchange.entity.AgencyExchangeProduct;
import com.coddicted.buzzma.exchange.persistence.AgencyExchangeProductRepository;
import com.coddicted.buzzma.exchange.service.ExchangeProductService;
import com.coddicted.buzzma.shared.common.BaseCrudService;
import com.coddicted.buzzma.shared.exception.BusinessRuleViolationException;
import com.coddicted.buzzma.shared.exception.NotFoundException;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ExchangeProductServiceImpl extends BaseCrudService implements ExchangeProductService {

  private final AgencyExchangeProductRepository exchangeProductRepository;

  public ExchangeProductServiceImpl(
      final AgencyExchangeProductRepository exchangeProductRepository) {
    this.exchangeProductRepository = exchangeProductRepository;
  }

  @Override
  @Transactional(readOnly = true)
  public List<AgencyExchangeProduct> list(final UUID agencyId) {
    return this.exchangeProductRepository.findByAgencyIdAndIsDeletedFalseOrderByNameAsc(agencyId);
  }

  @Override
  @Transactional
  public AgencyExchangeProduct create(final AgencyExchangeProduct product, final UUID requesterId) {
    final String name = product.getName().trim();
    if (this.exchangeProductRepository.existsByAgencyIdAndNameIgnoreCaseAndIsDeletedFalse(
        requesterId, name)) {
      throw new BusinessRuleViolationException("Exchange product already exists: " + name);
    }
    final AgencyExchangeProduct toSave =
        product.toBuilder()
            .name(name)
            .agencyId(requesterId)
            .isDeleted(false)
            .createdBy(requesterId)
            .updatedBy(requesterId)
            .build();
    return this.exchangeProductRepository.save(toSave);
  }

  @Override
  @Transactional
  public AgencyExchangeProduct update(final AgencyExchangeProduct product, final UUID requesterId) {
    final AgencyExchangeProduct existing =
        this.exchangeProductRepository
            .findByIdAndAgencyIdAndIsDeletedFalse(product.getId(), requesterId)
            .orElseThrow(
                () -> new NotFoundException("Exchange product not found: " + product.getId()));
    final String name = product.getName().trim();
    if (!name.equalsIgnoreCase(existing.getName())
        && this.exchangeProductRepository.existsByAgencyIdAndNameIgnoreCaseAndIsDeletedFalse(
            requesterId, name)) {
      throw new BusinessRuleViolationException("Exchange product already exists: " + name);
    }
    return this.exchangeProductRepository.save(
        existing.toBuilder().name(name).updatedBy(requesterId).build());
  }

  @Override
  @Transactional
  public AgencyExchangeProduct delete(final UUID id, final UUID requesterId) {
    final AgencyExchangeProduct existing =
        this.exchangeProductRepository
            .findByIdAndAgencyIdAndIsDeletedFalse(id, requesterId)
            .orElseThrow(() -> new NotFoundException("Exchange product not found: " + id));
    return this.exchangeProductRepository.save(
        existing.toBuilder().isDeleted(true).updatedBy(requesterId).build());
  }
}
