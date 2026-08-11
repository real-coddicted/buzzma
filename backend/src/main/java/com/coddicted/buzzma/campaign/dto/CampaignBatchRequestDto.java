package com.coddicted.buzzma.campaign.dto;

import java.util.Set;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class CampaignBatchRequestDto {

  Set<UUID> ids;
}
