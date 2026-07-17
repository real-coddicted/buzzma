package com.coddicted.buzzma.claim.utils;

import com.coddicted.buzzma.claim.entity.Claim;
import com.coddicted.buzzma.extraction.entity.ScoredValue;
import com.coddicted.buzzma.shared.constants.BuzzmahConstants;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

public class ClaimUtils {
  private ClaimUtils() {}

  public static ExtractedScoredResult updateExtractedDataForMatchWithManualEntryInOrder(
      Claim claim, Map<String, ScoredValue> extractedDetails, Integer overallScore) {

    Map<String, ScoredValue> details =
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
      Claim claim, Map<String, ScoredValue> extractedDetails, Integer overallScore) {

    Map<String, ScoredValue> details =
        extractedDetails != null ? new HashMap<>(extractedDetails) : new HashMap<>();

    processPlatform(claim, details);
    processAccountName(claim, details);
    processProductName(claim, details);

    return new ExtractedScoredResult(details, overallScore);
  }

  public static ExtractedScoredResult updateExtractedDataForMatchWithManualEntryInReview(
      Claim claim, Map<String, ScoredValue> extractedDetails, Integer overallScore) {

    Map<String, ScoredValue> details =
        extractedDetails != null ? new HashMap<>(extractedDetails) : new HashMap<>();

    processPlatform(claim, details);
    processAccountName(claim, details);
    processProductName(claim, details);
    overallScore = processReviewUrl(claim, details, overallScore);

    return new ExtractedScoredResult(details, overallScore);
  }

  public static ExtractedScoredResult updateExtractedDataForMatchWithManualEntryInReturn(
      Claim claim, Map<String, ScoredValue> extractedDetails, Integer overallScore) {

    Map<String, ScoredValue> details =
        extractedDetails != null ? new HashMap<>(extractedDetails) : new HashMap<>();

    processPlatform(claim, details);
    processAccountName(claim, details);
    processProductName(claim, details);

    return new ExtractedScoredResult(details, overallScore);
  }

  private static void processPlatform(Claim claim, Map<String, ScoredValue> details) {
    ScoredValue scoredValue = details.getOrDefault(BuzzmahConstants.PLATFORM, new ScoredValue());
    scoredValue.setMismatch(
        !claim.getPlatform().name().equalsIgnoreCase(scoredValue.getExtractedValue()));
    details.put(BuzzmahConstants.PLATFORM, scoredValue);
  }

  // Only penalize when the screenshot extracted an orderId that doesn't match;
  // absence of orderId in extracted details means this screenshot type doesn't carry it.
  private static Integer processOrderId(
      Claim claim, Map<String, ScoredValue> details, Integer overallScore) {
    ScoredValue scoredValue = details.getOrDefault(BuzzmahConstants.ORDER_ID, new ScoredValue());
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

  private static void processOrderDate(Claim claim, Map<String, ScoredValue> details) {
    ScoredValue scoredValue = details.getOrDefault(BuzzmahConstants.ORDER_DATE, new ScoredValue());
    if (scoredValue.getExtractedValue() != null) {
      scoredValue.setMismatch(
          !String.valueOf(claim.getOrderDate())
              .equals(scoredValue.getExtractedValue().replaceAll("-", "")));
    } else {
      scoredValue.setMismatch(true);
    }
    details.put(BuzzmahConstants.ORDER_DATE, scoredValue);
  }

  private static void processAmount(Claim claim, Map<String, ScoredValue> details) {
    ScoredValue scoredValue = details.getOrDefault(BuzzmahConstants.AMOUNT, new ScoredValue());
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

  private static void processOrderedBy(Claim claim, Map<String, ScoredValue> details) {
    ScoredValue scoredValue = details.getOrDefault(BuzzmahConstants.ORDERED_BY, new ScoredValue());
    if (claim.getAccountName() == null) {
      scoredValue.setMismatch(true);
    } else {
      scoredValue.setMismatch(
          !claim.getAccountName().equalsIgnoreCase(scoredValue.getExtractedValue()));
    }
    details.put(BuzzmahConstants.ORDERED_BY, scoredValue);
  }

  private static void processProductName(Claim claim, Map<String, ScoredValue> details) {
    ScoredValue scoredValue =
        details.getOrDefault(BuzzmahConstants.PRODUCT_NAME, new ScoredValue());
    if (claim.getProductName() == null) {
      scoredValue.setMismatch(true);
    } else {
      scoredValue.setMismatch(
          !claim.getProductName().equalsIgnoreCase(scoredValue.getExtractedValue()));
    }
    details.put(BuzzmahConstants.PRODUCT_NAME, scoredValue);
  }

  private static void processSellerName(Claim claim, Map<String, ScoredValue> details) {
    ScoredValue scoredValue = details.getOrDefault(BuzzmahConstants.SELLER_NAME, new ScoredValue());
    if (claim.getSellerName() == null) {
      scoredValue.setMismatch(true);
    } else {
      scoredValue.setMismatch(
          !claim.getSellerName().equalsIgnoreCase(scoredValue.getExtractedValue()));
    }
    details.put(BuzzmahConstants.SELLER_NAME, scoredValue);
  }

  private static Integer processReviewUrl(
      Claim claim, Map<String, ScoredValue> details, Integer overallScore) {
    ScoredValue scoredValue = details.getOrDefault(BuzzmahConstants.REVIEW_URL, new ScoredValue());
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

  private static void processAccountName(Claim claim, Map<String, ScoredValue> details) {
    ScoredValue scoredValue =
        details.getOrDefault(BuzzmahConstants.ACCOUNT_NAME, new ScoredValue());
    if (claim.getAccountName() == null) {
      scoredValue.setMismatch(true);
    } else {
      scoredValue.setMismatch(
          !claim.getAccountName().equalsIgnoreCase(scoredValue.getExtractedValue()));
    }
    details.put(BuzzmahConstants.ACCOUNT_NAME, scoredValue);
  }

  public record ExtractedScoredResult(Map<String, ScoredValue> extractedResult, int overallScore) {}
}
