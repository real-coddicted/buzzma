package com.coddicted.buzzma.claim.policy;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.coddicted.buzzma.campaign.entity.Campaign;
import com.coddicted.buzzma.campaign.entity.CampaignType;
import com.coddicted.buzzma.campaign.entity.ExchangeProduct;
import com.coddicted.buzzma.claim.entity.Claim;
import com.coddicted.buzzma.shared.exception.BusinessRuleViolationException;
import java.util.List;
import org.junit.jupiter.api.Test;

class ClaimPolicyTest {

  @Test
  void testValidateExchangeProductAcceptsConfiguredProductOnExchangeCampaign() {
    final Campaign campaign =
        Campaign.builder()
            .type(CampaignType.CAMPAIGN_TYPE_EXCHANGE)
            .exchangeProducts(List.of(ExchangeProduct.builder().productName("Widget").build()))
            .build();

    assertDoesNotThrow(
        () ->
            ClaimPolicy.validateExchangeProduct(
                campaign, Claim.builder().exchangeProduct("Widget").build()));
  }

  @Test
  void testValidateExchangeProductRequiresProductOnExchangeCampaign() {
    final Campaign campaign =
        Campaign.builder()
            .type(CampaignType.CAMPAIGN_TYPE_EXCHANGE)
            .exchangeProducts(List.of(ExchangeProduct.builder().productName("Widget").build()))
            .build();

    final BusinessRuleViolationException ex =
        assertThrows(
            BusinessRuleViolationException.class,
            () -> ClaimPolicy.validateExchangeProduct(campaign, Claim.builder().build()));
    assertEquals("Exchange product is required for exchange campaigns", ex.getMessage());
  }

  @Test
  void testValidateExchangeProductRejectsUnconfiguredProductOnExchangeCampaign() {
    final Campaign campaign =
        Campaign.builder()
            .type(CampaignType.CAMPAIGN_TYPE_EXCHANGE)
            .exchangeProducts(List.of(ExchangeProduct.builder().productName("Widget").build()))
            .build();

    final BusinessRuleViolationException ex =
        assertThrows(
            BusinessRuleViolationException.class,
            () ->
                ClaimPolicy.validateExchangeProduct(
                    campaign, Claim.builder().exchangeProduct("Gadget").build()));
    assertEquals(
        "Exchange product must be one of the campaign's configured exchange products",
        ex.getMessage());
  }

  @Test
  void testValidateExchangeProductRejectsProductOnNonExchangeCampaign() {
    final Campaign campaign = Campaign.builder().type(CampaignType.CAMPAIGN_TYPE_REVIEW).build();

    final BusinessRuleViolationException ex =
        assertThrows(
            BusinessRuleViolationException.class,
            () ->
                ClaimPolicy.validateExchangeProduct(
                    campaign, Claim.builder().exchangeProduct("Widget").build()));
    assertEquals("Exchange product is only allowed on exchange campaigns", ex.getMessage());
  }

  @Test
  void testValidateExchangeProductAcceptsNoProductOnNonExchangeCampaign() {
    final Campaign campaign = Campaign.builder().type(CampaignType.CAMPAIGN_TYPE_REVIEW).build();

    assertDoesNotThrow(
        () -> ClaimPolicy.validateExchangeProduct(campaign, Claim.builder().build()));
  }
}
