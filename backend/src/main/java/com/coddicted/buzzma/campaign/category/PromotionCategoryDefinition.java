package com.coddicted.buzzma.campaign.category;

import com.coddicted.buzzma.campaign.entity.CampaignStepType;
import com.coddicted.buzzma.campaign.entity.CampaignType;
import com.coddicted.buzzma.campaign.entity.PromotionCategory;
import com.coddicted.buzzma.shared.enums.Platform;
import java.util.List;
import java.util.Set;

/**
 * Declares the shape of a single promotion category: which platforms, campaign types, and claim
 * steps it allows, and which step every campaign in the category is forced to require. One
 * implementation per {@link PromotionCategory}, registered in {@link
 * PromotionCategoryDefinitionRegistry}.
 */
public interface PromotionCategoryDefinition {

  PromotionCategory category();

  Set<Platform> allowedPlatforms();

  Set<CampaignType> allowedCampaignTypes();

  /** The steps a campaign owner can select from when configuring required screenshots. */
  List<CampaignStepType> allowedSteps();

  /** The step every campaign in this category is forced to require, regardless of selection. */
  CampaignStepType forcedStep();
}
