package com.coddicted.buzzma.campaign.processor;

import com.coddicted.buzzma.campaign.dto.CampaignRequestDto;
import com.coddicted.buzzma.campaign.entity.Campaign;
import com.coddicted.buzzma.campaign.entity.CampaignAssignment;
import com.coddicted.buzzma.campaign.entity.CampaignSlot;
import com.coddicted.buzzma.campaign.entity.Product;
import com.coddicted.buzzma.shared.util.FileUtils;
import java.util.UUID;

final class Fixtures {

  static final UUID REQUESTER_ID = UUID.fromString("55555555-5555-5555-5555-555555555555");
  static final UUID CAMPAIGN_ID_1 = UUID.fromString("11111111-1111-1111-1111-111111111111");
  static final UUID ASSIGNEE_ID = UUID.fromString("88888888-8888-8888-8888-888888888888");

  static final CampaignRequestDto REQUEST_MIXED_SLOT_OFFERED =
      FileUtils.loadResourceAsObject(
          "/fixtures/input/campaign/campaign-request-mixed-slot-offered.json",
          CampaignRequestDto.class);

  static final Product PRODUCT_1 =
      FileUtils.loadResourceAsObject("/fixtures/input/campaign/product-1.json", Product.class);

  static final Campaign CAMPAIGN_1 =
      FileUtils.loadResourceAsObject("/fixtures/input/campaign/campaign-1.json", Campaign.class);

  static final Campaign CAMPAIGN_1_PUBLISHED =
      FileUtils.loadResourceAsObject(
          "/fixtures/output/campaign/campaign-1-published.json", Campaign.class);

  static final CampaignSlot EXPECTED_SLOT =
      FileUtils.loadResourceAsObject(
          "/fixtures/output/campaign/campaign-slot-mixed-slot-offered.json", CampaignSlot.class);

  static final CampaignAssignment EXPECTED_ASSIGNMENT =
      FileUtils.loadResourceAsObject(
          "/fixtures/output/campaign/campaign-assignment-mixed-slot-offered.json",
          CampaignAssignment.class);

  private Fixtures() {}
}
