package com.coddicted.buzzma.claim.service.impl;

import com.coddicted.buzzma.campaign.entity.Campaign;
import com.coddicted.buzzma.campaign.entity.Deal;
import com.coddicted.buzzma.claim.dto.PendingR2UploadView;
import com.coddicted.buzzma.claim.entity.Claim;
import com.coddicted.buzzma.claim.entity.ClaimScreenshot;
import com.coddicted.buzzma.claim.entity.ScreenshotType;
import com.coddicted.buzzma.extraction.entity.ScoredValue;
import com.coddicted.buzzma.shared.enums.Platform;
import com.coddicted.buzzma.shared.util.FileUtils;
import com.coddicted.buzzma.storage.event.R2UploadMessage;
import java.math.BigInteger;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

final class Fixtures {

  static final String ECOMMERCE_ORDER_ID = "403-1234567-8901234";
  static final Platform PLATFORM = Platform.PLATFORM_AMAZON;

  static final UUID CLAIM_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
  static final UUID OWNER_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
  static final UUID NON_OWNER_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
  static final UUID DEAL_ID = UUID.fromString("44444444-4444-4444-4444-444444444444");
  static final UUID SLOT_ID = UUID.fromString("55555555-5555-5555-5555-555555555555");
  static final UUID SCREENSHOT_ID = UUID.fromString("66666666-6666-6666-6666-666666666666");

  static final BigInteger AMOUNT_APPROVED_PAISE = BigInteger.valueOf(10000L);

  static final String CLAIM_CODE = "CLM1-A2B3";
  static final String REVIEWER_COMMENTS = "Screenshot does not match the order";

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

  static final UUID R2_CAMPAIGN_ID = UUID.fromString("77777777-7777-7777-7777-777777777777");
  static final UUID R2_DELETED_CAMPAIGN_ID =
      UUID.fromString("88888888-8888-8888-8888-888888888888");
  static final UUID R2_ORPHAN_SCREENSHOT_ID =
      UUID.fromString("99999999-9999-9999-9999-999999999999");
  static final String R2_CAMPAIGN_CODE = "CMP1-R2X9";
  static final Instant R2_CREATED_BEFORE = Instant.parse("2026-10-01T10:00:00Z");
  static final int R2_MAX_ATTEMPTS = 5;
  static final int R2_BATCH_SIZE = 500;

  static final PendingR2UploadView PENDING_R2_UPLOAD =
      new PendingR2UploadView(
          SCREENSHOT_ID,
          CLAIM_ID,
          CLAIM_CODE,
          R2_CAMPAIGN_ID,
          SCREENSHOT_KEY,
          ScreenshotType.SCREENSHOT_TYPE_ORDER);

  static final PendingR2UploadView PENDING_R2_UPLOAD_DELETED_CAMPAIGN =
      new PendingR2UploadView(
          R2_ORPHAN_SCREENSHOT_ID,
          CLAIM_ID,
          CLAIM_CODE,
          R2_DELETED_CAMPAIGN_ID,
          SCREENSHOT_KEY,
          ScreenshotType.SCREENSHOT_TYPE_ORDER);

  static final Campaign R2_CAMPAIGN =
      Campaign.builder().id(R2_CAMPAIGN_ID).code(R2_CAMPAIGN_CODE).build();

  static final R2UploadMessage EXPECTED_R2_UPLOAD_MESSAGE =
      new R2UploadMessage(
          SCREENSHOT_ID,
          CLAIM_ID,
          R2_CAMPAIGN_CODE,
          CLAIM_CODE,
          SCREENSHOT_KEY,
          "SCREENSHOT_TYPE_ORDER");

  private Fixtures() {}
}
