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
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CampaignRepository extends JpaRepository<Campaign, UUID> {

  Optional<Campaign> findByIdAndIsDeletedFalse(UUID id);

  Optional<Campaign> findByIdAndOwnerId(UUID id, UUID ownerId);

  Optional<Campaign> findByCodeAndIsDeletedFalse(String code);

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

  @Modifying
  @Query(
      """
      UPDATE Campaign c SET c.isDeleted = true
      WHERE c.ownerId = :ownerId
        AND c.id = :campaignId
        AND c.status = CampaignStatus.CAMPAIGN_STATUS_DRAFT
      """)
  int deleteDraftCampaign(@Param("ownerId") UUID ownerId, @Param("campaignId") UUID campaignId);

  @Query(
      """
      SELECT c FROM Campaign c
      WHERE c.isDeleted = false
        AND c.status IN :statuses
        AND c.endDate < :today
      """)
  List<Campaign> findExpiredCampaigns(
      @Param("statuses") List<CampaignStatus> statuses,
      @Param("today") Integer today,
      Pageable pageable);

  @Query(
      value =
          """
          SELECT
              c.id         AS campaignId,
              c.title      AS campaignTitle,
              c.code       AS code,
              c.platform   AS platform,
              c.type       AS campaignType,
              p.brand_name AS productBrandName,
              p.image_url  AS productImageUrl,
              c.start_date AS startDate,
              c.end_date   AS endDate,
              c.campaign_price_paise AS campaignPricePaise,
              cs.id              AS slotId,
              cs.slots_available AS slotsAvailable,
              cs.total_slots     AS totalSlots
          FROM campaigns c
          JOIN products p ON c.product_id = p.id
          JOIN campaign_slots cs ON c.id = cs.campaign_id AND cs.is_deleted = false
          WHERE c.owner_id = :ownerId
            AND c.open_to_all = true
            AND c.is_deleted = false
            AND c.end_date >= :today
            AND c.status IN ('CAMPAIGN_STATUS_PAUSED', 'CAMPAIGN_STATUS_ACTIVE')
            AND c.id NOT IN (
              SELECT campaign_id FROM campaign_assignments
              WHERE assignor_id = :ownerId
                AND assignee_id = :assigneeId
                AND is_deleted = false
            )
          ORDER BY c.start_date ASC
          """,
      nativeQuery = true)
  List<AssignableCampaignView> findAssignableCampaigns(
      @Param("ownerId") UUID ownerId,
      @Param("assigneeId") UUID assigneeId,
      @Param("today") Integer today);

  @Query(
      value =
          """
          SELECT
              c.id         AS campaignId,
              c.title      AS campaignTitle,
              c.code       AS code,
              c.platform   AS platform,
              c.type       AS campaignType,
              p.brand_name AS productBrandName,
              p.image_url  AS productImageUrl,
              c.start_date AS startDate,
              c.end_date   AS endDate,
              c.campaign_price_paise AS campaignPricePaise,
              cs.slots_available AS slotsAvailable,
              cs.total_slots     AS totalSlots
          FROM campaigns c
          JOIN products p ON c.product_id = p.id
          JOIN campaign_slots cs ON c.id = cs.campaign_id AND cs.is_deleted = false
          WHERE c.owner_id = :ownerId
            AND c.is_deleted = false
            AND c.end_date >= :today
            AND c.status IN ('CAMPAIGN_STATUS_PAUSED', 'CAMPAIGN_STATUS_ACTIVE')
            AND c.id NOT IN (SELECT campaign_id FROM campaign_shares)
          ORDER BY c.start_date ASC
          """,
      nativeQuery = true)
  List<ShareableCampaignView> findShareableCampaigns(
      @Param("ownerId") UUID ownerId, @Param("today") Integer today);
}
