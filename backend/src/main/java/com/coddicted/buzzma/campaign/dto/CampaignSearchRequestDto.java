package com.coddicted.buzzma.campaign.dto;

import com.coddicted.buzzma.campaign.entity.CampaignStatus;
import com.coddicted.buzzma.campaign.entity.CampaignType;
import com.coddicted.buzzma.shared.enums.Platform;
import jakarta.annotation.Nullable;
import java.util.List;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class CampaignSearchRequestDto {

  @Nullable List<String> brands;

  @Nullable List<Platform> platforms;

  @Nullable List<CampaignType> types;

  @Nullable List<CampaignStatus> statuses;

  @Nullable Integer fromDate;

  @Nullable Integer toDate;
}
