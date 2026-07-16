package com.coddicted.buzzma.campaign.persistence;

import com.coddicted.buzzma.campaign.entity.Campaign;
import com.coddicted.buzzma.campaign.entity.CampaignStatus;
import com.coddicted.buzzma.campaign.entity.CampaignType;
import com.coddicted.buzzma.shared.enums.Platform;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CampaignRepository extends JpaRepository<Campaign, UUID> {

  Optional<Campaign> findByIdAndIsDeletedFalse(UUID id);

  Optional<Campaign> findByIdAndOwnerId(UUID id, UUID ownerId);

  Set<Campaign> findByIdInAndIsDeletedFalse(Set<UUID> ids);

  List<Campaign> findByOwnerIdAndIsDeletedFalse(UUID ownerId);

  Page<Campaign> findByOwnerIdAndIsDeletedFalse(UUID ownerId, Pageable pageable);

  @Query(
      value =
          """
          SELECT c FROM Campaign c
            JOIN c.product p
          WHERE c.ownerId = :ownerId
            AND c.isDeleted = false
            AND (:brands IS NULL OR LOWER(p.brandName) IN :brands)
            AND (:platforms IS NULL OR c.platform IN :platforms)
            AND (:types IS NULL OR c.type IN :types)
            AND (:statuses IS NULL OR c.status IN :statuses)
            AND (
              ((:fromDate IS NULL OR c.startDate >= :fromDate)
                AND (:toDate IS NULL OR c.startDate <= :toDate))
              OR
              ((:fromDate IS NULL OR c.endDate >= :fromDate)
                AND (:toDate IS NULL OR c.endDate <= :toDate))
            )
          ORDER BY c.startDate DESC NULLS LAST
          """,
      countQuery =
          """
          SELECT COUNT(c) FROM Campaign c
            JOIN c.product p
          WHERE c.ownerId = :ownerId
            AND c.isDeleted = false
            AND (:brands IS NULL OR LOWER(p.brandName) IN :brands)
            AND (:platforms IS NULL OR c.platform IN :platforms)
            AND (:types IS NULL OR c.type IN :types)
            AND (:statuses IS NULL OR c.status IN :statuses)
            AND (
              ((:fromDate IS NULL OR c.startDate >= :fromDate)
                AND (:toDate IS NULL OR c.startDate <= :toDate))
              OR
              ((:fromDate IS NULL OR c.endDate >= :fromDate)
                AND (:toDate IS NULL OR c.endDate <= :toDate))
            )
          """)
  Page<Campaign> search(
      @Param("ownerId") UUID ownerId,
      @Param("brands") List<String> brands,
      @Param("platforms") List<Platform> platforms,
      @Param("types") List<CampaignType> types,
      @Param("statuses") List<CampaignStatus> statuses,
      @Param("fromDate") Integer fromDate,
      @Param("toDate") Integer toDate,
      Pageable pageable);

  @Query(
      """
      SELECT DISTINCT p.brandName FROM Campaign c
        JOIN c.product p
      WHERE c.ownerId = :ownerId
        AND c.isDeleted = false
      ORDER BY p.brandName
      """)
  List<String> findDistinctBrandNamesByOwnerId(@Param("ownerId") UUID ownerId);
}
