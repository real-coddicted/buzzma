package com.coddicted.buzzma.campaign.dto;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ShareCampaignResponseDto {
  UUID campaignId;
  UUID toUserId;
  UUID fromUserId;
  Instant createdAt;
}
