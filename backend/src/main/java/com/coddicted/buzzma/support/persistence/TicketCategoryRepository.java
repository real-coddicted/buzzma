package com.coddicted.buzzma.support.persistence;

import com.coddicted.buzzma.support.entity.TicketCategory;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketCategoryRepository extends JpaRepository<TicketCategory, UUID> {

  List<TicketCategory> findAllByIsDeletedFalse();

  Optional<TicketCategory> findByIdAndIsDeletedFalse(UUID id);
}
