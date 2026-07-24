package com.coddicted.buzzma.claim.utils;

import com.coddicted.buzzma.campaign.entity.Campaign;
import com.coddicted.buzzma.claim.client.ExtractedScoredResult;
import com.coddicted.buzzma.claim.entity.Claim;
import com.coddicted.buzzma.extraction.entity.ScoredValue;
import com.coddicted.buzzma.shared.constants.BuzzmahConstants;
import com.coddicted.buzzma.shared.score.PayloadItem;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ClaimScreenshotScorerUtils {

  private static final Logger LOGGER = LoggerFactory.getLogger(ClaimScreenshotScorerUtils.class);

  private ClaimScreenshotScorerUtils() {}

  public static double scoreOrderDate(final String orderDate, final Campaign campaign) {
    if (orderDate == null || campaign.getStartDate() == null) {
      return 0.0;
    }
    final int dateInt;
    try {
      final LocalDate date = LocalDate.parse(orderDate);
      dateInt = date.getYear() * 10000 + date.getMonthValue() * 100 + date.getDayOfMonth();
    } catch (final DateTimeParseException e) {
      LOGGER.warn("scoreOrderDate: could not parse orderDate '{}': {}", orderDate, e.getMessage());
      return 0.0;
    }
    if (dateInt < campaign.getStartDate()) {
      return 0.0;
    }
    if (campaign.getEndDate() != null && dateInt > campaign.getEndDate()) {
      return 0.0;
    }
    return 1.0;
  }

  /**
   * Returns the minimum of the locally-computed orderDate score and the Score API's per-field
   * scores, so a single low-confidence field pulls the overall score down.
   */
  public static int combineOverallScore(
      final int orderDateScore, final Map<String, ScoredValue> apiScores) {

    final java.util.OptionalInt apiMin =
        apiScores.values().stream()
            .map(ScoredValue::getScore)
            .filter(Objects::nonNull)
            .mapToInt(Integer::intValue)
            .min();

    return apiMin.isPresent() ? Math.min(orderDateScore, apiMin.getAsInt()) : orderDateScore;
  }

  /**
   * Builds the platform/productName payload items common to every screenshot type, plus whatever
   * additional items (e.g. accountName, sellerName, reviewUrl) the caller supplies via {@link
   * #payloadItem}.
   */
  public static List<PayloadItem> buildPayload(
      final String platform,
      final String productName,
      final Campaign campaign,
      final List<PayloadItem> additionalItems) {
    final List<PayloadItem> payload = new ArrayList<>();
    payload.add(
        PayloadItem.builder()
            .label(BuzzmahConstants.PLATFORM)
            .expected(campaign.getPlatform() != null ? campaign.getPlatform().name() : "")
            .actual(platform != null ? platform : "")
            .build());
    payload.add(
        PayloadItem.builder()
            .label(BuzzmahConstants.PRODUCT_NAME)
            .expected(campaign.getProduct().getName())
            .actual(productName != null ? productName : "")
            .build());
    payload.addAll(additionalItems);
    return payload;
  }

  public static PayloadItem payloadItem(
      final String label, final String expected, final String actual) {
    return PayloadItem.builder()
        .label(label)
        .expected(expected != null ? expected : "")
        .actual(actual != null ? actual : "")
        .build();
  }

  public static ExtractedScoredResult updateExtractedDataForMatchWithManualEntryInOrder(
      final Claim claim, final Map<String, ScoredValue> extractedDetails, Integer overallScore) {

    final Map<String, ScoredValue> details =
        extractedDetails != null ? new HashMap<>(extractedDetails) : new HashMap<>();

    processPlatform(claim, details);
    overallScore = processOrderId(claim, details, overallScore);
    processOrderDate(claim, details);
    processAmount(claim, details);
    processOrderedBy(claim, details);
    processProductName(claim, details);
    processSellerName(claim, details);

    return new ExtractedScoredResult(details, overallScore);
  }

  public static ExtractedScoredResult updateExtractedDataForMatchWithManualEntryInRating(
      final Claim claim,
      final Map<String, ScoredValue> extractedDetails,
      final Integer overallScore) {

    final Map<String, ScoredValue> details =
        extractedDetails != null ? new HashMap<>(extractedDetails) : new HashMap<>();

    processPlatform(claim, details);
    processAccountName(claim, details);
    processProductName(claim, details);

    return new ExtractedScoredResult(details, overallScore);
  }

  public static ExtractedScoredResult updateExtractedDataForMatchWithManualEntryInReview(
      final Claim claim, final Map<String, ScoredValue> extractedDetails, Integer overallScore) {

    final Map<String, ScoredValue> details =
        extractedDetails != null ? new HashMap<>(extractedDetails) : new HashMap<>();

    processPlatform(claim, details);
    processAccountName(claim, details);
    processProductName(claim, details);
    overallScore = processReviewUrl(claim, details, overallScore);

    return new ExtractedScoredResult(details, overallScore);
  }

  public static ExtractedScoredResult updateExtractedDataForMatchWithManualEntryInReturn(
      final Claim claim,
      final Map<String, ScoredValue> extractedDetails,
      final Integer overallScore) {

    final Map<String, ScoredValue> details =
        extractedDetails != null ? new HashMap<>(extractedDetails) : new HashMap<>();

    processPlatform(claim, details);
    processAccountName(claim, details);
    processProductName(claim, details);

    return new ExtractedScoredResult(details, overallScore);
  }

  private static void processPlatform(final Claim claim, final Map<String, ScoredValue> details) {
    final ScoredValue scoredValue =
        details.getOrDefault(BuzzmahConstants.PLATFORM, new ScoredValue());
    scoredValue.setMismatch(
        !claim.getPlatform().name().equalsIgnoreCase(scoredValue.getExtractedValue()));
    details.put(BuzzmahConstants.PLATFORM, scoredValue);
  }

  // Only penalize when the screenshot extracted an orderId that doesn't match;
  // absence of orderId in extracted details means this screenshot type doesn't carry it.
  private static Integer processOrderId(
      final Claim claim, final Map<String, ScoredValue> details, Integer overallScore) {
    final ScoredValue scoredValue =
        details.getOrDefault(BuzzmahConstants.ORDER_ID, new ScoredValue());
    if (scoredValue.getExtractedValue() != null) {
      scoredValue.setMismatch(
          !scoredValue.getExtractedValue().equalsIgnoreCase(claim.getEcommerceOrderId()));
      if (scoredValue.isMismatch()) {
        scoredValue.setScore(0);
        overallScore = 0;
      }
    }
    details.put(BuzzmahConstants.ORDER_ID, scoredValue);
    return overallScore;
  }

  private static void processOrderDate(final Claim claim, final Map<String, ScoredValue> details) {
    final ScoredValue scoredValue =
        details.getOrDefault(BuzzmahConstants.ORDER_DATE, new ScoredValue());
    if (scoredValue.getExtractedValue() != null) {
      scoredValue.setMismatch(
          !String.valueOf(claim.getOrderDate())
              .equals(scoredValue.getExtractedValue().replaceAll("-", "")));
    } else {
      scoredValue.setMismatch(true);
    }
    details.put(BuzzmahConstants.ORDER_DATE, scoredValue);
  }

  private static void processAmount(final Claim claim, final Map<String, ScoredValue> details) {
    final ScoredValue scoredValue =
        details.getOrDefault(BuzzmahConstants.AMOUNT, new ScoredValue());
    if (claim.getAmountPaise() == null || scoredValue.getExtractedValue() == null) {
      scoredValue.setMismatch(true);
    } else {
      scoredValue.setMismatch(
          !claim
              .getAmountPaise()
              .equals(new BigInteger(scoredValue.getExtractedValue().replace(".", ""))));
    }
    details.put(BuzzmahConstants.AMOUNT, scoredValue);
  }

  private static void processOrderedBy(final Claim claim, final Map<String, ScoredValue> details) {
    final ScoredValue scoredValue =
        details.getOrDefault(BuzzmahConstants.ORDERED_BY, new ScoredValue());
    if (claim.getAccountName() == null) {
      scoredValue.setMismatch(true);
    } else {
      scoredValue.setMismatch(
          !claim.getAccountName().equalsIgnoreCase(scoredValue.getExtractedValue()));
    }
    details.put(BuzzmahConstants.ORDERED_BY, scoredValue);
  }

  private static void processProductName(
      final Claim claim, final Map<String, ScoredValue> details) {
    final ScoredValue scoredValue =
        details.getOrDefault(BuzzmahConstants.PRODUCT_NAME, new ScoredValue());
    if (claim.getProductName() == null) {
      scoredValue.setMismatch(true);
    } else {
      scoredValue.setMismatch(
          !claim.getProductName().equalsIgnoreCase(scoredValue.getExtractedValue()));
    }
    details.put(BuzzmahConstants.PRODUCT_NAME, scoredValue);
  }

  private static void processSellerName(final Claim claim, final Map<String, ScoredValue> details) {
    final ScoredValue scoredValue =
        details.getOrDefault(BuzzmahConstants.SELLER_NAME, new ScoredValue());
    if (claim.getSellerName() == null) {
      scoredValue.setMismatch(true);
    } else {
      scoredValue.setMismatch(
          !claim.getSellerName().equalsIgnoreCase(scoredValue.getExtractedValue()));
    }
    details.put(BuzzmahConstants.SELLER_NAME, scoredValue);
  }

  private static Integer processReviewUrl(
      final Claim claim, final Map<String, ScoredValue> details, Integer overallScore) {
    final ScoredValue scoredValue =
        details.getOrDefault(BuzzmahConstants.REVIEW_URL, new ScoredValue());
    if (claim.getReviewUrl() == null) {
      scoredValue.setMismatch(true);
      scoredValue.setScore(0);
      overallScore = 0;
    } else {
      scoredValue.setMismatch(
          !claim.getReviewUrl().equalsIgnoreCase(scoredValue.getExtractedValue()));
      if (scoredValue.isMismatch()) {
        scoredValue.setScore(0);
        overallScore = 0;
      }
    }
    details.put(BuzzmahConstants.REVIEW_URL, scoredValue);
    return overallScore;
  }

  private static void processAccountName(
      final Claim claim, final Map<String, ScoredValue> details) {
    final ScoredValue scoredValue =
        details.getOrDefault(BuzzmahConstants.ACCOUNT_NAME, new ScoredValue());
    if (claim.getAccountName() == null) {
      scoredValue.setMismatch(true);
    } else {
      scoredValue.setMismatch(
          !claim.getAccountName().equalsIgnoreCase(scoredValue.getExtractedValue()));
    }
    details.put(BuzzmahConstants.ACCOUNT_NAME, scoredValue);
  }
}
