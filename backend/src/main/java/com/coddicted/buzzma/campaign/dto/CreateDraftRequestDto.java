package com.coddicted.buzzma.campaign.dto;

import com.coddicted.buzzma.campaign.entity.CampaignStepType;
import com.coddicted.buzzma.campaign.entity.CampaignType;
import com.coddicted.buzzma.campaign.entity.ExchangeProduct;
import com.coddicted.buzzma.campaign.entity.PromotionCategory;
import com.coddicted.buzzma.campaign.entity.Reward;
import com.coddicted.buzzma.shared.enums.Platform;
import jakarta.annotation.Nullable;
import java.math.BigInteger;
import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

/**
 * Request body for saving a campaign draft. Unlike {@link CampaignRequestDto}, every field is
 * optional — a draft may be incomplete, and no validation is performed until launch.
 */
@Value
@Builder
@Jacksonized
public class CreateDraftRequestDto {

  @Nullable String title;

  @Nullable UUID ownerId;

  @Nullable Platform platform;

  @Nullable @Builder.Default PromotionCategory category = PromotionCategory.ECOMMERCE;

  @Nullable String productName;

  @Nullable String productImageUrl;

  @Nullable String productUrl;

  @Nullable String productBrandName;

  @Nullable BigInteger originalPricePaise;

  @Nullable Integer startDate;

  @Nullable Integer endDate;

  @Nullable CampaignType campaignType;

  @Nullable BigInteger campaignPricePaise;

  @Nullable Integer totalSlots;

  @Nullable Integer returnWindowDays;

  @Nullable List<CampaignAssignmentRequestDto> assignees;

  boolean openToAll;

  boolean affiliateLinkAllowed;

  @Nullable BigInteger commissionToAllPaise;

  @Nullable String termsAndConditions;

  @Nullable String sellerName;

  @Nullable List<CampaignStepType> requiredSteps;

  @Nullable List<Reward> rewards;

  @Nullable List<ExchangeProduct> exchangeProducts;
}
