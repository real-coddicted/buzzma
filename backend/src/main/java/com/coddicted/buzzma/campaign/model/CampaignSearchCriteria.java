package com.coddicted.buzzma.campaign.model;

import com.coddicted.buzzma.campaign.entity.CampaignStatus;
import com.coddicted.buzzma.campaign.entity.CampaignType;
import com.coddicted.buzzma.shared.enums.Platform;
import java.util.List;

public record CampaignSearchCriteria(
    List<String> brands,
    List<Platform> platforms,
    List<CampaignType> types,
    List<CampaignStatus> statuses,
    Integer fromDate,
    Integer toDate) {}
