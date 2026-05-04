package com.coddicted.buzzma.support.service;

import com.coddicted.buzzma.support.entity.TicketCategory;
import com.coddicted.buzzma.support.entity.TicketSubCategory;
import java.util.List;
import java.util.UUID;

public interface TicketCategoryService {

  List<TicketCategory> listActiveCategories();

  List<TicketSubCategory> listActiveSubCategoriesByCategoryId(UUID categoryId);

  TicketCategory getById(UUID categoryId);

  TicketSubCategory getTicketSubCategory(UUID categoryId, UUID subCategoryId);
}
