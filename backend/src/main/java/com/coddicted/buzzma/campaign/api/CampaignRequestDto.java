package com.coddicted.buzzma.campaign.api;

import com.coddicted.buzzma.campaign.entity.CampaignType;
import com.coddicted.buzzma.campaign.entity.OwnerType;
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

  @NotBlank
  OwnerType ownerType;

  @NotBlank String platform;

  @NotBlank String productBrandName;

  @NotBlank String productImageUrl;

  @NotBlank String productUrl;

  @NotBlank BigInteger originalPricePaise;

  @Nullable Integer endDate;

  @Nullable
  CampaignType campaignType;

  Long totalSlots;

  List<CampaignAssignmentRequestDto> assignees;

  boolean openToAll;
}
