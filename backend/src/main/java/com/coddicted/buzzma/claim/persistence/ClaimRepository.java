package com.coddicted.buzzma.claim.persistence;

import com.coddicted.buzzma.claim.entity.Claim;
import com.coddicted.buzzma.claim.entity.ClaimStatus;
import com.coddicted.buzzma.claim.model.ClaimReviewModel;
import com.coddicted.buzzma.shared.enums.Platform;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ClaimRepository extends JpaRepository<Claim, UUID> {

  List<Claim> findByOwnerIdAndIsDeletedFalse(UUID ownerId);

  Optional<Claim> findByIdAndIsDeletedFalse(UUID id);

  Optional<Claim> findByIdAndOwnerIdAndIsDeletedFalse(UUID id, UUID ownerId);

  boolean existsByEcommerceOrderIdAndPlatformAndIsDeletedFalse(
      String ecommerceOrderId, Platform platform);

  @Query(
      value =
          """
          SELECT new com.coddicted.buzzma.claim.model.ClaimReviewModel(
            c, d.campaign, d.ownerId, m.name, b.name)
          FROM Claim c
            JOIN Deal d ON d.id = c.dealId
            JOIN BuzzmaUser b ON b.id = c.ownerId
            JOIN BuzzmaUser m ON m.id = d.ownerId
          WHERE d.ownerId = :mediatorId AND c.isDeleted = false AND d.isDeleted = false
            AND (:campaignIds IS NULL OR c.campaignId IN :campaignIds)
            AND (:claimStatuses IS NULL OR c.status IN :claimStatuses)
          ORDER BY c.updatedAt DESC
          """,
      countQuery =
          """
          SELECT COUNT(c)
          FROM Claim c
            JOIN Deal d ON d.id = c.dealId
          WHERE d.ownerId = :mediatorId AND c.isDeleted = false AND d.isDeleted = false
            AND (:campaignIds IS NULL OR c.campaignId IN :campaignIds)
            AND (:claimStatuses IS NULL OR c.status IN :claimStatuses)
          """)
  Page<ClaimReviewModel> findClaimsToReviewForMediator(
      @Param("mediatorId") UUID mediatorId,
      @Param("campaignIds") Collection<UUID> campaignIds,
      @Param("claimStatuses") Collection<ClaimStatus> claimStatuses,
      Pageable pageable);

  @Query(
      value =
          """
          SELECT new com.coddicted.buzzma.claim.model.ClaimReviewModel(
            c, d.campaign, d.ownerId, m.name, b.name)
          FROM Claim c
            JOIN Deal d ON d.id = c.dealId
            JOIN BuzzmaUser b ON b.id = c.ownerId
            JOIN BuzzmaUser m ON m.id = d.ownerId
          WHERE c.campaignId IN :campaignIds AND c.isDeleted = false AND d.isDeleted = false
            AND (:mediatorIds IS NULL OR d.ownerId IN :mediatorIds)
            AND (:claimStatuses IS NULL OR c.status IN :claimStatuses)
          ORDER BY c.updatedAt DESC
          """,
      countQuery =
          """
          SELECT COUNT(c)
          FROM Claim c
            JOIN Deal d ON d.id = c.dealId
          WHERE c.campaignId IN :campaignIds AND c.isDeleted = false AND d.isDeleted = false
            AND (:mediatorIds IS NULL OR d.ownerId IN :mediatorIds)
            AND (:claimStatuses IS NULL OR c.status IN :claimStatuses)
          """)
  Page<ClaimReviewModel> findClaimsToReviewForCampaigns(
      @Param("campaignIds") Collection<UUID> campaignIds,
      @Param("mediatorIds") Collection<UUID> mediatorIds,
      @Param("claimStatuses") Collection<ClaimStatus> claimStatuses,
      Pageable pageable);

  @Modifying
  @Query(
      nativeQuery = true,
      value =
          """
          UPDATE claims
          SET score = (
              SELECT MIN(s.score) FROM claim_screenshots s
              WHERE s.claim_id = :claimId AND s.is_deleted = false AND s.score IS NOT NULL
          )
          WHERE id = :claimId
          """)
  void updateScoreFromScreenshots(@Param("claimId") UUID claimId);
}
