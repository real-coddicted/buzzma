package com.coddicted.buzzma.campaign.dto;

import com.coddicted.buzzma.campaign.entity.CampaignStatus;
import com.coddicted.buzzma.campaign.entity.CampaignType;
import com.coddicted.buzzma.campaign.entity.Platform;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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

  @NotBlank Platform platform;

  @NotBlank String productName;

  @NotBlank String productImageUrl;

  @NotBlank String productUrl;

  @NotBlank BigInteger originalPricePaise;

  @Nullable Integer endDate;

  @NotNull CampaignType campaignType;

  @NotNull CampaignStatus campaignStatus;

  @NotNull Integer totalSlots;

  List<CampaignAssignmentRequestDto> assignees;

  boolean openToAll;
}
