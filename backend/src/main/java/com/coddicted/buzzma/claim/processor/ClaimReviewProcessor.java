package com.coddicted.buzzma.claim.processor;

import com.coddicted.buzzma.claim.dto.ClaimReviewResponseDto;
import com.coddicted.buzzma.claim.mapper.ClaimReviewMapper;
import com.coddicted.buzzma.claim.service.ClaimReviewService;
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
      final UUID requesterId, final Pageable pageable) {
    return this.claimReviewService
        .getClaimReviews(requesterId, pageable)
        .map(this.claimReviewMapper::toResponse);
  }
}
