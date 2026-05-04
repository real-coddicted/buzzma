package com.coddicted.buzzma.support.service.impl;

import com.coddicted.buzzma.shared.common.BaseCrudService;
import com.coddicted.buzzma.shared.exception.NotFoundException;
import com.coddicted.buzzma.support.entity.TicketCategory;
import com.coddicted.buzzma.support.entity.TicketSubCategory;
import com.coddicted.buzzma.support.persistence.TicketCategoryRepository;
import com.coddicted.buzzma.support.persistence.TicketSubCategoryRepository;
import com.coddicted.buzzma.support.service.TicketCategoryService;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TicketCategoryServiceImpl extends BaseCrudService implements TicketCategoryService {

  private final TicketCategoryRepository categoryRepository;
  private final TicketSubCategoryRepository subCategoryRepository;

  public TicketCategoryServiceImpl(
      final TicketCategoryRepository categoryRepository,
      final TicketSubCategoryRepository subCategoryRepository) {
    this.categoryRepository = categoryRepository;
    this.subCategoryRepository = subCategoryRepository;
  }

  @Override
  @Transactional(readOnly = true)
  public List<TicketCategory> listActiveCategories() {
    return this.categoryRepository.findAllByIsDeletedFalse();
  }

  @Override
  @Transactional(readOnly = true)
  public List<TicketSubCategory> listActiveSubCategoriesByCategoryId(final UUID categoryId) {
    return this.subCategoryRepository.findAllByCategoryIdAndIsDeletedFalse(categoryId);
  }

  @Override
  @Transactional(readOnly = true)
  public TicketCategory getById(final UUID categoryId) {
    return mustFind(this.categoryRepository, categoryId, "TicketCategory");
  }

  @Override
  public TicketSubCategory getTicketSubCategory(final UUID categoryId, final UUID subCategoryId) {
    return this.subCategoryRepository
        .findByIdAndCategoryIdAndIsDeletedFalse(subCategoryId, categoryId)
        .orElseThrow(
            () ->
                new NotFoundException(
                    "Sub-category not found for id: "
                        + subCategoryId
                        + " and category id: "
                        + categoryId));
  }
}
