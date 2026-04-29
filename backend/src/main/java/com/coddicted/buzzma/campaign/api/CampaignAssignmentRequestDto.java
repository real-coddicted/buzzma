package com.coddicted.buzzma.campaign.api;

import com.coddicted.buzzma.campaign.entity.AssigneeType;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.math.BigInteger;

@Value
@Builder
@Jacksonized
public class CampaignAssignmentRequestDto {

    @NotBlank
    String assigneeCode;

    @NotBlank
    AssigneeType assigneeType;

    BigInteger campaignPricePaise = BigInteger.ZERO;

    BigInteger commissionOfferedPaise = BigInteger.ZERO;

    Long slotOffered = 0L;
}
