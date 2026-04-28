package com.coddicted.buzzma.campaign.api;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class DealRequestDto {

  UUID campaignId;

  @NotBlank String mediatorCode;

  @NotBlank String title;

  @Nullable String description;

  @NotBlank String image;

  @NotBlank String productUrl;

  @NotBlank String platform;

  @NotBlank String brandName;

  @NotBlank String dealType;

  Integer originalPricePaise;

  Integer pricePaise;

  Integer commissionPaise;

  Integer payoutPaise;

  @Nullable Double rating;

  @Nullable String category;

  @Nullable Boolean active;
}
