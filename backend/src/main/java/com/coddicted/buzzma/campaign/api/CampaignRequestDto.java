package com.coddicted.buzzma.campaign.api;

import com.coddicted.buzzma.campaign.entity.CampaignType;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;

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

  @NotBlank UUID ownerId;

  @NotBlank UUID ownerType;

  @NotBlank String platform;

  @NotBlank String productBrandName;

  @NotBlank String productImageUrl;

  @NotBlank String productUrl;

  @NotBlank BigInteger originalPricePaise;

  @NotBlank BigInteger campaignPricePaise;

  @NotBlank BigInteger commissionOfferedPaise;

  @Nullable Integer returnWindowDays;

  @Nullable
  CampaignType campaignType;

  Long totalSlots;

  @Nullable
  List<String> allowedAgencies;

  @Nullable Boolean openToAll;
}
