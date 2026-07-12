package com.coddicted.buzzma.claim.processor;

import com.coddicted.buzzma.claim.dto.ClaimReviewResponseDto;
import com.coddicted.buzzma.claim.entity.ClaimStatus;
import com.coddicted.buzzma.claim.mapper.ClaimReviewMapper;
import com.coddicted.buzzma.claim.service.ClaimReviewService;
import com.coddicted.buzzma.identity.entity.BuzzmaUser;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
public class ClaimReviewProcessor {

  private final ClaimReviewService claimReviewService;
  private final ClaimReviewMapper claimReviewMapper;

  public ClaimReviewProcessor(
      final ClaimReviewService claimReviewService, final ClaimReviewMapper claimReviewMapper) {
    this.claimReviewService = claimReviewService;
    this.claimReviewMapper = claimReviewMapper;
  }

  public Page<ClaimReviewResponseDto> listClaimReviews(
      final BuzzmaUser requester,
      final Set<UUID> campaignIdsFilter,
      final Set<UUID> mediatorIdsFilter,
      final Set<ClaimStatus> claimStatusFilter,
      final Pageable pageable) {
    return this.claimReviewService
        .getClaimReviews(
            requester, campaignIdsFilter, mediatorIdsFilter, claimStatusFilter, pageable)
        .map(this.claimReviewMapper::toResponse);
  }
}
