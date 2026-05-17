package com.coddicted.buzzma.campaign.service.impl;

import com.coddicted.buzzma.campaign.entity.Campaign;
import com.coddicted.buzzma.campaign.entity.CampaignAssignment;
import com.coddicted.buzzma.util.FileUtils;
import java.util.Set;
import java.util.UUID;

final class Fixtures {

  static final UUID CAMPAIGN_ID_1 = UUID.fromString("11111111-1111-1111-1111-111111111111");
  static final UUID CAMPAIGN_ID_2 = UUID.fromString("22222222-2222-2222-2222-222222222222");
  static final UUID CAMPAIGN_ID_3 = UUID.fromString("33333333-3333-3333-3333-333333333333");
  static final UUID OWNER_ID = UUID.fromString("44444444-4444-4444-4444-444444444444");
  static final UUID REQUESTER_ID = UUID.fromString("55555555-5555-5555-5555-555555555555");
  static final UUID NON_OWNER_ID = UUID.fromString("66666666-6666-6666-6666-666666666666");
  static final UUID ASSIGNMENT_ID_1 = UUID.fromString("77777777-7777-7777-7777-777777777777");
  static final UUID SAVED_CAMPAIGN_ID = UUID.fromString("99999999-9999-9999-9999-999999999999");

  static final Campaign CAMPAIGN_1 =
      FileUtils.loadResourceAsObject("/fixtures/input/campaign/campaign-1.json", Campaign.class);

  static final Campaign CAMPAIGN_2 =
      FileUtils.loadResourceAsObject("/fixtures/input/campaign/campaign-2.json", Campaign.class);

  static final Campaign CAMPAIGN_3 =
      FileUtils.loadResourceAsObject("/fixtures/input/campaign/campaign-3.json", Campaign.class);

  static final Campaign EXPECTED_CAMPAIGN_1 =
      FileUtils.loadResourceAsObject(
          "/fixtures/output/campaign/campaign-1.json", Campaign.class);

  static final CampaignAssignment ASSIGNMENT_1 =
      FileUtils.loadResourceAsObject(
          "/fixtures/input/campaign/campaign-assignment-1.json", CampaignAssignment.class);

  static final Set<UUID> CAMPAIGN_ID_SET = Set.of(CAMPAIGN_ID_1, CAMPAIGN_ID_2);
  static final Set<Campaign> CAMPAIGN_SET = Set.of(CAMPAIGN_1, CAMPAIGN_2);

  private Fixtures() {}
}
