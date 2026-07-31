package com.coddicted.buzzma.campaign.persistence;

import java.math.BigInteger;
import java.util.UUID;

public interface AssignableCampaignView {

  UUID getCampaignId();

  String getCampaignTitle();

  Integer getStartDate();

  Integer getEndDate();

  BigInteger getCampaignPricePaise();

  UUID getSlotId();

  Integer getSlotsAvailable();

  Integer getTotalSlots();
}
