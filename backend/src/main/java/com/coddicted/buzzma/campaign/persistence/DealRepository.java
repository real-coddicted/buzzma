package com.coddicted.buzzma.campaign.persistence;

import com.coddicted.buzzma.campaign.entity.Campaign;
import com.coddicted.buzzma.campaign.entity.CampaignStatus;
import com.coddicted.buzzma.campaign.entity.CampaignType;
import com.coddicted.buzzma.campaign.entity.Deal;
import com.coddicted.buzzma.shared.enums.Platform;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface DealRepository {
    @Query(
            "SELECT c FROM Campaign c WHERE c.status = :status AND c.isDeleted = false"
                    + " AND (:type IS NULL OR c.type = :type)"
                    + " AND (:platform IS NULL OR c.platform = :platform)")
    Page<Campaign> findActiveDeals(
            @Param("status") CampaignStatus status,
            @Param("type") CampaignType type,
            @Param("platform") Platform platform,
            Pageable pageable);

    Page<Deal> findByOwnerIdAndStatusIsPublished(UUID ownerId, Pageable pageable);

}
