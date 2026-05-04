package com.coddicted.buzzma.support.persistence;

import com.coddicted.buzzma.support.entity.TicketSubCategory;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketSubCategoryRepository extends JpaRepository<TicketSubCategory, UUID> {

  List<TicketSubCategory> findAllByCategoryIdAndIsDeletedFalse(UUID categoryId);

  Optional<TicketSubCategory> findByIdAndCategoryIdAndIsDeletedFalse(UUID id, UUID categoryId);
}
