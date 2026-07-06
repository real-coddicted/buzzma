package com.coddicted.buzzma.claim.persistence;

import com.coddicted.buzzma.claim.entity.Claim;
import com.coddicted.buzzma.claim.model.ClaimReviewModel;
import com.coddicted.buzzma.shared.enums.Platform;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
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
          ORDER BY c.updatedAt DESC
          """,
      countQuery =
          """
          SELECT COUNT(c)
          FROM Claim c
            JOIN Deal d ON d.id = c.dealId
          WHERE d.ownerId = :mediatorId AND c.isDeleted = false AND d.isDeleted = false
          """)
  Page<ClaimReviewModel> findClaimsToReviewForMediator(
      @Param("mediatorId") UUID mediatorId, Pageable pageable);

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
          ORDER BY c.updatedAt DESC
          """,
      countQuery =
          """
          SELECT COUNT(c)
          FROM Claim c
            JOIN Deal d ON d.id = c.dealId
          WHERE c.campaignId IN :campaignIds AND c.isDeleted = false AND d.isDeleted = false
          """)
  Page<ClaimReviewModel> findClaimsToReviewForCampaigns(
      @Param("campaignIds") Collection<UUID> campaignIds, Pageable pageable);
}
