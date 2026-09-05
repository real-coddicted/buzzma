package com.coddicted.buzzma.campaign.dto;

import com.coddicted.buzzma.campaign.entity.CampaignAction;
import com.coddicted.buzzma.campaign.entity.CampaignStatus;
import com.coddicted.buzzma.campaign.entity.CampaignStepType;
import com.coddicted.buzzma.campaign.entity.CampaignType;
import com.coddicted.buzzma.campaign.entity.ExchangeProduct;
import com.coddicted.buzzma.campaign.entity.Reward;
import com.coddicted.buzzma.shared.enums.Platform;
import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigInteger;
import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class CampaignRequestDto {

  @NotBlank String title;

  @NotNull UUID ownerId;

  @NotNull Platform platform;

  @NotBlank String productName;

  @NotBlank String productImageUrl;

  @NotBlank String productUrl;

  @NotBlank String productBrandName;

  @NotNull BigInteger originalPricePaise;

  @Nullable Integer startDate;

  @Nullable Integer endDate;

  @NotNull CampaignType campaignType;

  @NotNull CampaignStatus campaignStatus;

  @NotNull BigInteger campaignPricePaise;

  @NotNull @Positive Integer totalSlots;

  @Nullable Integer returnWindowDays;

  @Valid List<CampaignAssignmentRequestDto> assignees;

  boolean openToAll;

  boolean affiliateLinkAllowed;

  @Nullable BigInteger commissionToAllPaise;

  @Nullable String termsAndConditions;

  @Nullable String sellerName;

  @Nullable List<CampaignStepType> requiredSteps;

  @Nullable List<Reward> rewards;

  @Nullable List<ExchangeProduct> exchangeProducts;

  @Nullable CampaignAction action;
}
