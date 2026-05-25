package com.coddicted.buzzma.campaign.persistence;

import com.coddicted.buzzma.campaign.entity.Deal;
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
          SELECT * FROM deals d
          WHERE d.owner_id = :ownerId
            AND d.is_deleted = false
            AND d.id NOT IN (
              SELECT c.deal_id FROM claims c
              WHERE c.owner_id = :requesterId AND c.is_deleted = false
            )
          """,
      countQuery =
          """
          SELECT COUNT(*) FROM deals d
          WHERE d.owner_id = :ownerId
            AND d.is_deleted = false
            AND d.id NOT IN (
              SELECT c.deal_id FROM claims c
              WHERE c.owner_id = :requesterId AND c.is_deleted = false
            )
          """,
      nativeQuery = true)
  Page<Deal> findUnclaimedDeals(
      @Param("ownerId") UUID ownerId, @Param("requesterId") UUID requesterId, Pageable pageable);
}
