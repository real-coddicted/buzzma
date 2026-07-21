package com.coddicted.buzzma.campaign.dto;

import java.util.UUID;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class CampaignOptionDto {

  UUID id;

  String title;

  String code;
}
