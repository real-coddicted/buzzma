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
 * Request body for updating a campaign draft. Every field is optional; fields left {@code null}
 * leave the previously stored draft value unchanged. No validation is performed until launch.
 */
@Value
@Builder
@Jacksonized
public class UpdateDraftRequestDto {

  @Nullable String title;

  @Nullable UUID ownerId;

  @Nullable Platform platform;

  @Nullable PromotionCategory category;

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

  @Nullable Boolean openToAll;

  @Nullable Boolean affiliateLinkAllowed;

  @Nullable BigInteger commissionToAllPaise;

  @Nullable String termsAndConditions;

  @Nullable String sellerName;

  @Nullable List<CampaignStepType> requiredSteps;

  @Nullable List<Reward> rewards;

  @Nullable List<ExchangeProduct> exchangeProducts;
}
