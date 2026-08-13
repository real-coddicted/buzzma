package com.coddicted.buzzma.campaign.persistence;

import com.coddicted.buzzma.campaign.entity.Campaign;
import com.coddicted.buzzma.identity.entity.BuzzmaUser;
import com.coddicted.buzzma.shared.util.FileUtils;
import java.util.UUID;

final class Fixtures {

  static final UUID BRAND_ID = UUID.fromString("bb444444-4444-4444-4444-444444444444");
  static final UUID OTHER_BRAND_ID = UUID.fromString("bb555555-5555-5555-5555-555555555555");

  static final BuzzmaUser AGENCY_USER =
      FileUtils.loadResourceAsObject(
          "/fixtures/input/identity/agency-shared-campaigns-user.json", BuzzmaUser.class);

  static final Campaign SHARED_CAMPAIGN_1 =
      FileUtils.loadResourceAsObject(
          "/fixtures/input/campaign/shared-campaign-1.json", Campaign.class);

  static final Campaign SHARED_CAMPAIGN_2 =
      FileUtils.loadResourceAsObject(
          "/fixtures/input/campaign/shared-campaign-2.json", Campaign.class);

  static final Campaign SHARED_CAMPAIGN_3 =
      FileUtils.loadResourceAsObject(
          "/fixtures/input/campaign/shared-campaign-3.json", Campaign.class);

  static final Campaign SHARED_CAMPAIGN_4 =
      FileUtils.loadResourceAsObject(
          "/fixtures/input/campaign/shared-campaign-4.json", Campaign.class);

  static final Campaign SHARED_CAMPAIGN_5 =
      FileUtils.loadResourceAsObject(
          "/fixtures/input/campaign/shared-campaign-5.json", Campaign.class);

  static final Campaign SHARED_CAMPAIGN_6 =
      FileUtils.loadResourceAsObject(
          "/fixtures/input/campaign/shared-campaign-6.json", Campaign.class);

  private Fixtures() {}
}
