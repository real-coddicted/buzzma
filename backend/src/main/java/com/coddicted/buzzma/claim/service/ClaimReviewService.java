package com.coddicted.buzzma.claim.service;

import com.coddicted.buzzma.claim.entity.ClaimReviewStatus;
import com.coddicted.buzzma.claim.entity.ClaimStatus;
import com.coddicted.buzzma.claim.model.ClaimReviewModel;
import com.coddicted.buzzma.identity.entity.BuzzmaUser;
import com.coddicted.buzzma.shared.enums.Platform;
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
      Set<String> brandsFilter,
      Set<Platform> platformsFilter,
      Set<ClaimReviewStatus> reviewStatusesFilter,
      Pageable pageable);
}
