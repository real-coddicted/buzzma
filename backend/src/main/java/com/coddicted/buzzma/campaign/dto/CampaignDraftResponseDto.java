package com.coddicted.buzzma.campaign.dto;

import com.coddicted.buzzma.campaign.entity.CampaignStatus;
import com.coddicted.buzzma.campaign.entity.CampaignStepType;
import com.coddicted.buzzma.campaign.entity.CampaignType;
import com.coddicted.buzzma.campaign.entity.ExchangeProduct;
import com.coddicted.buzzma.campaign.entity.PromotionCategory;
import com.coddicted.buzzma.campaign.entity.Reward;
import com.coddicted.buzzma.shared.enums.Platform;
import java.math.BigInteger;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

/**
 * The response shape for a not-yet-launched campaign draft. A draft has no persisted {@code
 * Product} or {@code CampaignAssignment} rows, so unlike {@link CampaignResponseDto} this mirrors
 * the flat request fields (e.g. {@code productUrl}, {@code assignees}) rather than the post-launch
 * shape.
 */
@Value
@Builder(toBuilder = true)
@Jacksonized
public class CampaignDraftResponseDto {

  UUID id;

  String code;

  String title;

  UUID ownerId;

  Platform platform;

  PromotionCategory category;

  String productName;

  String productImageUrl;

  String productUrl;

  String productBrandName;

  BigInteger originalPricePaise;

  Integer startDate;

  Integer endDate;

  CampaignType campaignType;

  CampaignStatus status;

  BigInteger campaignPricePaise;

  Integer totalSlots;

  Integer returnWindowDays;

  List<CampaignAssignmentRequestDto> assignees;

  boolean openToAll;

  boolean affiliateLinkAllowed;

  BigInteger commissionToAllPaise;

  String termsAndConditions;

  String sellerName;

  List<CampaignStepType> requiredSteps;

  List<Reward> rewards;

  List<ExchangeProduct> exchangeProducts;

  Instant createdAt;

  UUID createdBy;

  Instant updatedAt;

  UUID updatedBy;
}
