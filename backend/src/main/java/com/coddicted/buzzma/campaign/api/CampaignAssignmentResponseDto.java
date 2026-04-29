package com.coddicted.buzzma.campaign.api;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.math.BigInteger;
import java.util.UUID;

@Value
@Builder
@Jacksonized
public class CampaignAssignmentResponseDto {
    UUID assignorId;
    UUID assigneeId;
    BigInteger campaignPricePaise;
    BigInteger commissionOfferedPaise;
    Long slotOffered;
}
