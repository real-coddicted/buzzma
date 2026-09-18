package com.coddicted.buzzma.claim.policy;

import com.coddicted.buzzma.campaign.entity.Campaign;
import com.coddicted.buzzma.campaign.entity.CampaignType;
import com.coddicted.buzzma.claim.entity.Claim;
import com.coddicted.buzzma.shared.exception.BusinessRuleViolationException;

public final class ClaimPolicy {
  private ClaimPolicy() {}

  public static void validateExchangeProduct(final Campaign campaign, final Claim claim) {
    final String exchangeProduct = claim.getExchangeProduct();
    if (campaign.getType() == CampaignType.CAMPAIGN_TYPE_EXCHANGE) {
      if (exchangeProduct == null || exchangeProduct.isBlank()) {
        throw new BusinessRuleViolationException(
            "Exchange product is required for exchange campaigns");
      }
      final boolean isConfigured =
          campaign.getExchangeProducts() != null
              && campaign.getExchangeProducts().stream()
                  .anyMatch(product -> exchangeProduct.equals(product.getProductName()));
      if (!isConfigured) {
        throw new BusinessRuleViolationException(
            "Exchange product must be one of the campaign's configured exchange products");
      }
    } else if (exchangeProduct != null && !exchangeProduct.isBlank()) {
      throw new BusinessRuleViolationException(
          "Exchange product is only allowed on exchange campaigns");
    }
  }
}
