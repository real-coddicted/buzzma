package com.coddicted.buzzma.campaign.dto;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class SharedCampaignViewResponseDto {

  String campaignName;

  String campaignCode;

  UUID sharedWithUserId;

  String sharedWithUserName;

  String sharedWithUserCode;

  Instant sharedAt;
}
