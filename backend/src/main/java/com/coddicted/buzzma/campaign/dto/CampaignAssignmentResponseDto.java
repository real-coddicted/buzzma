package com.coddicted.buzzma.campaign.dto;

import java.math.BigInteger;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class CampaignAssignmentResponseDto {
  UUID assignorId;
  UUID assigneeId;
  String assigneeName;
  BigInteger adjustedCampaignPricePaise;
  BigInteger commissionOfferedPaise;
  Long slotOffered;
}
