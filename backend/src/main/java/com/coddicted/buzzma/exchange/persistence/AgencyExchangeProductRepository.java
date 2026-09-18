package com.coddicted.buzzma.exchange.persistence;

import com.coddicted.buzzma.exchange.entity.AgencyExchangeProduct;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AgencyExchangeProductRepository
    extends JpaRepository<AgencyExchangeProduct, UUID> {

  List<AgencyExchangeProduct> findByAgencyIdAndIsDeletedFalseOrderByNameAsc(UUID agencyId);

  Optional<AgencyExchangeProduct> findByIdAndAgencyIdAndIsDeletedFalse(UUID id, UUID agencyId);

  boolean existsByAgencyIdAndNameIgnoreCaseAndIsDeletedFalse(UUID agencyId, String name);
}
