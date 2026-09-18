package com.coddicted.buzzma.campaign.entity;

import lombok.Getter;

/**
 * The top-level promotion vertical a campaign belongs to. Distinct from {@link CampaignType} (which
 * task the actor performs) and {@link com.coddicted.buzzma.shared.enums.Platform} (where) — both a
 * task type and a platform can be shared across multiple categories, so category is tracked as its
 * own field rather than derived from either.
 */
@Getter
public enum PromotionCategory {
  ECOMMERCE("Ecommerce"),
  QUICK_COMMERCE("Quick Commerce"),
  APP_PROMOTION("App Promotion");

  private final String displayName;

  PromotionCategory(final String displayName) {
    this.displayName = displayName;
  }
}
