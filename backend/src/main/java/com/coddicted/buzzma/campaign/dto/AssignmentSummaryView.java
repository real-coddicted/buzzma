package com.coddicted.buzzma.campaign.dto;

import com.coddicted.buzzma.campaign.entity.CampaignType;
import com.coddicted.buzzma.shared.enums.Platform;
import java.math.BigInteger;
import java.net.URL;
import java.util.UUID;

public record AssignmentSummaryView(
    UUID id,
    String productName,
    URL productImageUrl,
    Platform platform,
    CampaignType dealType,
    BigInteger originalPricePaise,
    BigInteger offeredPricePaise,
    Integer slotLimit) {}
