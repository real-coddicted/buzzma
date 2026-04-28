package com.coddicted.buzzma.campaign.api;

import jakarta.annotation.Nullable;
import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class DealResponseDto {

  UUID id;

  UUID campaignId;

  String mediatorCode;

  String title;

  String description;

  String image;

  String productUrl;

  String platform;

  String brandName;

  String dealType;

  Integer originalPricePaise;

  Integer pricePaise;

  Integer commissionPaise;

  Integer payoutPaise;

  Double rating;

  String category;

  Boolean active;

  Boolean isDeleted;

  @Nullable Instant createdAt;

  @Nullable Instant updatedAt;
}
