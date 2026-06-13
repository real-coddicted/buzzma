package com.coddicted.buzzma.claim.service;

import com.coddicted.buzzma.claim.model.ClaimReviewModel;
import com.coddicted.buzzma.identity.entity.BuzzmaUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ClaimReviewService {

  Page<ClaimReviewModel> getClaimReviews(BuzzmaUser requester, Pageable pageable);
}
