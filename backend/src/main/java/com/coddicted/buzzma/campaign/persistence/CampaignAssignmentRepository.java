package com.coddicted.buzzma.campaign.persistence;

import com.coddicted.buzzma.campaign.entity.CampaignAssignment;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CampaignAssignmentRepository extends JpaRepository<CampaignAssignment, UUID> {

  List<CampaignAssignment> findByCampaignId(UUID campaignId);

  List<CampaignAssignment> findByAssignorId(UUID assignorId);

  List<CampaignAssignment> findByAssigneeId(UUID assigneeId);

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
