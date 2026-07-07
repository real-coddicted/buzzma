package com.coddicted.buzzma.campaign.persistence;

import com.coddicted.buzzma.campaign.dto.AssignmentSummaryView;
import com.coddicted.buzzma.campaign.entity.CampaignAssignment;
import com.coddicted.buzzma.campaign.entity.CampaignAssignmentStatus;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CampaignAssignmentRepository extends JpaRepository<CampaignAssignment, UUID> {

  List<CampaignAssignment> findByCampaignId(UUID campaignId);

  List<CampaignAssignment> findByCampaignIdAndIsDeletedFalse(UUID campaignId);

  List<CampaignAssignment> findByAssignorId(UUID assignorId);

  List<CampaignAssignment> findByAssigneeIdAndStatus(
      UUID assigneeId, CampaignAssignmentStatus status);

  Page<CampaignAssignment> findByAssigneeIdAndStatusAndIsDeletedFalse(
      UUID assigneeId, CampaignAssignmentStatus status, Pageable pageable);

  @Query(
      value =
          """
          SELECT new com.coddicted.buzzma.campaign.dto.AssignmentSummaryView(
            ca.id, p.name, p.imageUrl, c.platform, c.type,
            p.pricePaise, ca.adjustedCampaignPricePaise, ca.slotLimit, c.status
          )
          FROM CampaignAssignment ca, Campaign c
          JOIN c.product p
          WHERE ca.campaignId = c.id
            AND ca.isDeleted = false AND c.isDeleted = false
            AND ca.assigneeId = :assigneeId AND ca.status = :status
            AND (:status <> com.coddicted.buzzma.campaign.entity.CampaignAssignmentStatus.CAMPAIGN_ASSIGNMENT_STATUS_LOCKED
                 OR c.status = com.coddicted.buzzma.campaign.entity.CampaignStatus.CAMPAIGN_STATUS_ACTIVE)
          """,
      countQuery =
          """
          SELECT COUNT(ca)
          FROM CampaignAssignment ca, Campaign c
          WHERE ca.campaignId = c.id
            AND ca.isDeleted = false AND c.isDeleted = false
            AND ca.assigneeId = :assigneeId AND ca.status = :status
            AND (:status <> com.coddicted.buzzma.campaign.entity.CampaignAssignmentStatus.CAMPAIGN_ASSIGNMENT_STATUS_LOCKED
                 OR c.status = com.coddicted.buzzma.campaign.entity.CampaignStatus.CAMPAIGN_STATUS_ACTIVE)
          """)
  Page<AssignmentSummaryView> findAssignmentSummaries(
      @Param("assigneeId") UUID assigneeId,
      @Param("status") CampaignAssignmentStatus status,
      Pageable pageable);

  @Query(
      value =
          """
                            SELECT COUNT(DISTINCT ca.campaign_id)
                            FROM campaign_assignments ca
                            JOIN campaigns c ON c.id = ca.campaign_id
                            WHERE ca.assigned_to_type = :assignedToType
                              AND ca.assigned_to_code = :assignedToCode
                              AND ca.is_deleted = false
                              AND c.is_deleted = false
                              AND c.status = 'CAMPAIGN_STATUS_ACTIVE'
                            """,
      nativeQuery = true)
  long countActiveCampaigns(
      @Param("assignedToType") String assignedToType,
      @Param("assignedToCode") String assignedToCode);
}
