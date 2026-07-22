package com.coddicted.buzzma.claim.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.coddicted.buzzma.claim.entity.Claim;
import com.coddicted.buzzma.claim.entity.ClaimReviewStatus;
import com.coddicted.buzzma.claim.model.ClaimReviewModel;
import org.junit.jupiter.api.Test;

class ClaimReviewMapperTest {

  private final ClaimReviewMapper mapper = new ClaimReviewMapperImpl();

  @Test
  void testToResponseDefaultsMediatorVerifiedToFalseWhenNull() {
    final Claim claim = Claim.builder().mediatorVerified(null).build();
    final ClaimReviewModel model = ClaimReviewModel.builder().claim(claim).build();

    assertFalse(mapper.toResponse(model).getMediatorVerified());
  }

  @Test
  void testToResponseDefaultsClaimReviewStatusToPendingWhenNull() {
    final Claim claim = Claim.builder().reviewStatus(null).build();
    final ClaimReviewModel model = ClaimReviewModel.builder().claim(claim).build();

    assertEquals(
        ClaimReviewStatus.CLAIM_REVIEW_STATUS_PENDING,
        mapper.toResponse(model).getClaimReviewStatus());
  }

  @Test
  void testToResponsePreservesExplicitMediatorVerifiedAndReviewStatus() {
    final Claim claim =
        Claim.builder()
            .mediatorVerified(true)
            .reviewStatus(ClaimReviewStatus.CLAIM_REVIEW_STATUS_APPROVED)
            .build();
    final ClaimReviewModel model = ClaimReviewModel.builder().claim(claim).build();

    assertEquals(true, mapper.toResponse(model).getMediatorVerified());
    assertEquals(
        ClaimReviewStatus.CLAIM_REVIEW_STATUS_APPROVED,
        mapper.toResponse(model).getClaimReviewStatus());
  }
}
