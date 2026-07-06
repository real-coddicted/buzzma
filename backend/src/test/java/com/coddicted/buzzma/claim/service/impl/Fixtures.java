package com.coddicted.buzzma.claim.service.impl;

import com.coddicted.buzzma.campaign.entity.CampaignType;
import com.coddicted.buzzma.campaign.entity.Deal;
import com.coddicted.buzzma.claim.entity.Claim;
import com.coddicted.buzzma.claim.entity.ClaimScreenshot;
import com.coddicted.buzzma.extraction.entity.ScoredValue;
import com.coddicted.buzzma.shared.enums.Platform;
import com.coddicted.buzzma.shared.util.FileUtils;
import java.util.Map;
import java.util.UUID;

final class Fixtures {

  static final CampaignType CAMPAIGN_TYPE = CampaignType.CAMPAIGN_TYPE_REVIEW;
  static final String ECOMMERCE_ORDER_ID = "403-1234567-8901234";
  static final Platform PLATFORM = Platform.PLATFORM_AMAZON;

  static final UUID CLAIM_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
  static final UUID OWNER_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
  static final UUID NON_OWNER_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
  static final UUID DEAL_ID = UUID.fromString("44444444-4444-4444-4444-444444444444");
  static final UUID SLOT_ID = UUID.fromString("55555555-5555-5555-5555-555555555555");

  static final String SCREENSHOT_KEY = "claims/order-screenshot.jpg";
  static final String SCREENSHOT_FILENAME = "screenshot.jpg";
  static final String CONTENT_TYPE = "image/jpeg";
  static final String REVIEW_URL = "https://example.com/review";
  static final byte[] SCREENSHOT_BYTES = {1, 2, 3};
  static final Map<String, ScoredValue> EXTRACTED_DETAILS =
      Map.of(
          "orderId",
              ScoredValue.builder().extractedValue("403-1234567-8901234").score(null).build(),
          "productName", ScoredValue.builder().extractedValue("Test Product").score(null).build());

  static final Claim CLAIM_1 =
      FileUtils.loadResourceAsObject("/fixtures/input/claim/claim-1.json", Claim.class);

  static final Claim CLAIM_2 =
      FileUtils.loadResourceAsObject("/fixtures/input/claim/claim-2.json", Claim.class);

  static final Claim CLAIM_3 =
      FileUtils.loadResourceAsObject("/fixtures/input/claim/claim-3.json", Claim.class);

  static final Claim CLAIM_INPUT =
      FileUtils.loadResourceAsObject("/fixtures/input/claim/claim-input.json", Claim.class);

  static final Deal DEAL_1 =
      FileUtils.loadResourceAsObject("/fixtures/input/claim/deal-1.json", Deal.class);

  static final ClaimScreenshot SCREENSHOT_1 =
      FileUtils.loadResourceAsObject(
          "/fixtures/input/claim/screenshot-1.json", ClaimScreenshot.class);

  private Fixtures() {}
}
