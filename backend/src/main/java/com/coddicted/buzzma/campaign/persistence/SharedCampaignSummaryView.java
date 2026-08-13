package com.coddicted.buzzma.campaign.persistence;

import java.time.Instant;
import java.util.UUID;

public interface SharedCampaignSummaryView {

  String getCampaignName();

  String getCampaignCode();

  UUID getSharedWithUserId();

  String getSharedWithUserName();

  String getSharedWithUserCode();

  Instant getSharedAt();
}
