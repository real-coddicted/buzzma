package com.coddicted.buzzma.claim.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.coddicted.buzzma.claim.entity.Claim;
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
  void testToResponsePreservesExplicitMediatorVerified() {
    final Claim claim = Claim.builder().mediatorVerified(true).build();
    final ClaimReviewModel model = ClaimReviewModel.builder().claim(claim).build();

    assertEquals(true, mapper.toResponse(model).getMediatorVerified());
  }

  @Test
  void testToResponseDefaultsBrandVerifiedToFalseWhenNull() {
    final Claim claim = Claim.builder().brandVerified(null).build();
    final ClaimReviewModel model = ClaimReviewModel.builder().claim(claim).build();

    assertFalse(mapper.toResponse(model).getBrandVerified());
  }

  @Test
  void testToResponsePreservesExplicitBrandVerified() {
    final Claim claim = Claim.builder().brandVerified(true).build();
    final ClaimReviewModel model = ClaimReviewModel.builder().claim(claim).build();

    assertEquals(true, mapper.toResponse(model).getBrandVerified());
  }

  @Test
  void testToResponseMapsAccountNameFromClaim() {
    final Claim claim = Claim.builder().accountName("Profile A").build();
    final ClaimReviewModel model = ClaimReviewModel.builder().claim(claim).build();

    assertEquals("Profile A", mapper.toResponse(model).getAccountName());
  }
}
