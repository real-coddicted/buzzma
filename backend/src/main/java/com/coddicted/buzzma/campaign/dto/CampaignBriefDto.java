package com.coddicted.buzzma.campaign.dto;

import com.coddicted.buzzma.shared.enums.Platform;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class CampaignBriefDto {

  UUID id;

  String title;

  String productBrandName;

  Platform platform;
}
