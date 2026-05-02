package com.coddicted.buzzma.campaign.predicate;

import com.coddicted.buzzma.campaign.entity.Campaign;
import java.util.function.Predicate;

public class CampaignPublishPredicate implements Predicate<Campaign> {
  @Override
  public boolean test(Campaign campaign) {
    return false;
  }
}
