package com.coddicted.buzzma.campaign.persistence;

import com.coddicted.buzzma.campaign.entity.Deal;
import java.util.Collection;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DealRepository extends JpaRepository<Deal, UUID> {

  @Query(
      value =
          """
          SELECT d.* FROM deals d, campaigns c
          WHERE d.owner_id IN (:ownerIds)
            AND d.is_deleted = false
            AND c.id = d.campaign_id
            AND c.status = 'CAMPAIGN_STATUS_ACTIVE'
          """,
      countQuery =
          """
          SELECT COUNT(d.*) FROM deals d, campaigns c
          WHERE d.owner_id IN (:ownerIds)
            AND d.is_deleted = false
            AND c.id = d.campaign_id
            AND c.status = 'CAMPAIGN_STATUS_ACTIVE'
          """,
      nativeQuery = true)
  Page<Deal> findActiveDeals(@Param("ownerIds") Collection<UUID> ownerIds, Pageable pageable);
}
