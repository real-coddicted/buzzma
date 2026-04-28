package com.coddicted.buzzma.campaign.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.coddicted.buzzma.campaign.entity.Deal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DealRepository extends JpaRepository<Deal, UUID> {

  Page<Deal> findAllByIsDeletedFalse(Pageable pageable);

  Optional<Deal> findByCampaignIdAndMediatorCode(UUID campaignId, String mediatorCode);

  Page<Deal> findAllByMediatorCodeAndIsDeletedFalse(String mediatorCode, Pageable pageable);

  List<Deal> findAllByMediatorCodeAndIsDeletedFalseAndActiveTrue(String mediatorCode);

  Page<Deal> findAllByMediatorCodeInAndIsDeletedFalse(
      List<String> mediatorCodes, Pageable pageable);

  @Query(
      value =
          "SELECT d.* FROM deals d"
              + " JOIN campaigns c ON d.campaign_id = c.id"
              + " WHERE d.mediator_code = :mediatorCode"
              + " AND d.active = true AND d.is_deleted = false"
              + " AND c.is_deleted = false AND c.status = 'active'"
              + " ORDER BY d.created_at DESC",
      countQuery =
          "SELECT COUNT(*) FROM deals d"
              + " JOIN campaigns c ON d.campaign_id = c.id"
              + " WHERE d.mediator_code = :mediatorCode"
              + " AND d.active = true AND d.is_deleted = false"
              + " AND c.is_deleted = false AND c.status = 'active'",
      nativeQuery = true)
  Page<Deal> findActiveProductsForMediator(
      @Param("mediatorCode") String mediatorCode, Pageable pageable);
}
