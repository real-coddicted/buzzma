package com.coddicted.buzzma.campaign.utils;

import com.coddicted.buzzma.campaign.entity.Campaign;

import java.util.UUID;

public final class CampaignUtil {
  private CampaignUtil() {}

  public static Campaign copy(final Campaign source, final UUID actorUserId) {
    Campaign copy = new Campaign();
    copy.setBrandUserId(source.getBrandUserId());
    copy.setBrandName(source.getBrandName());
    copy.setTitle(source.getTitle() + " (copy)");
    copy.setPlatform(source.getPlatform());
    copy.setImage(source.getImage());
    copy.setProductUrl(source.getProductUrl());
    copy.setOriginalPricePaise(source.getOriginalPricePaise());
    copy.setPricePaise(source.getPricePaise());
    copy.setPayoutPaise(source.getPayoutPaise());
    copy.setTotalSlots(source.getTotalSlots());
    copy.setUsedSlots(0);
    copy.setStatus(CampaignStatus.draft);
    copy.setDealType(source.getDealType());
    copy.setReturnWindowDays(source.getReturnWindowDays());
    copy.setAllowedAgencyCodes(
        source.getAllowedAgencyCodes() != null ? source.getAllowedAgencyCodes() : new String[0]);
    copy.setCreatedBy(actorUserId);
    copy.setUpdatedBy(actorUserId);
    return copy;
  }
}
