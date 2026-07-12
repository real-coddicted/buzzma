package com.coddicted.buzzma.claim.service;

import com.coddicted.buzzma.claim.entity.ClaimStatus;
import com.coddicted.buzzma.claim.model.ClaimReviewModel;
import com.coddicted.buzzma.identity.entity.BuzzmaUser;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ClaimReviewService {

  Page<ClaimReviewModel> getClaimReviews(
      BuzzmaUser requester,
      Set<UUID> campaignIdsFilter,
      Set<UUID> mediatorIdsFilter,
      Set<ClaimStatus> claimStatusFilter,
      Pageable pageable);
}
