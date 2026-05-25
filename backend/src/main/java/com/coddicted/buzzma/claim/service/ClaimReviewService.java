package com.coddicted.buzzma.claim.service;

import com.coddicted.buzzma.claim.model.ClaimReviewModel;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ClaimReviewService {

  Page<ClaimReviewModel> getClaimReviews(UUID requesterId, Pageable pageable);
}
