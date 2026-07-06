package com.coddicted.buzzma.extraction.service;

import com.coddicted.buzzma.shared.enums.Platform;
import java.util.Arrays;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class GeminiExtractionPromptBuilder {

  private static final String PLATFORM_VALUES =
      Arrays.stream(Platform.values()).map(Enum::name).collect(Collectors.joining("|"));

  private static final String PROMPT =
      """
      You are an order-data extractor. Analyze the provided e-commerce order screenshot and \
      return ONLY valid JSON with no markdown fences, no extra text, and no explanation. \
      The JSON must match this exact schema:
      {
        "platform": "<%s|null>",
        "orderId": "<order identifier string or null>",
        "orderDate": "<YYYY-MM-DD or null>",
        "productName": "<product name string or null>",
        "sellerName": "<seller or sold-by name string or null>",
        "amount": <total order amount as a number without currency symbol, or null>,
        "orderedBy": "<customer full name or null>"
      }
      Use null for any field that cannot be clearly determined from the image."""
          .formatted(PLATFORM_VALUES);

  private static final String RATING_PROMPT =
      """
      You are a rating-data extractor. Analyze the provided screenshot of a 5-star rating UI \
      and return ONLY valid JSON with no markdown fences, no extra text, and no explanation. \
      The JSON must match this exact schema:
      {
        "platform": "<%s|null>",
        "productName": "<product name string or null>",
        "accountName": "<the account or user name visible in the screenshot, or null>",
        "rating": <the numeric star rating given by the user as an integer between 1 and 5, or null>
      }
      Use null for any field that cannot be clearly determined from the image."""
          .formatted(PLATFORM_VALUES);

  private static final String REVIEW_PROMPT =
      """
      You are a review-data extractor. Analyze the provided screenshot of a product review and \
      return ONLY valid JSON with no markdown fences, no extra text, and no explanation. \
      The JSON must match this exact schema:
      {
        "platform": "<%s|null>",
        "productName": "<product name string or null>",
        "reviewText": "<full text of the review provided by the customer, or null>",
        "accountName": "<the account or user name of the reviewer, or null>",
        "reviewDate": "<date the review was posted in YYYY-MM-DD format, or null>",
        "reviewUrl": "<the URL visible in the browser's address bar, or null if the address bar is not visible in the screenshot>"
      }
      Use null for any field that cannot be clearly determined from the image."""
          .formatted(PLATFORM_VALUES);

  private static final String RETURN_PROMPT =
      """
      You are a return-window-data extractor. Analyze the provided screenshot showing product \
      return information and return ONLY valid JSON with no markdown fences, no extra text, and \
      no explanation. The JSON must match this exact schema:
      {
        "platform": "<%s|null>",
        "productName": "<product name string or null>",
        "accountName": "<the account or user name visible in the screenshot, or null>",
        "returnWindowClosedText": "<the exact text or label confirming the return window has closed, or null>",
        "returnWindowClosedDate": "<the date when the return window closed in YYYY-MM-DD format, or null>"
      }
      Use null for any field that cannot be clearly determined from the image."""
          .formatted(PLATFORM_VALUES);

  public String build() {
    return PROMPT;
  }

  public String buildRatingPrompt() {
    return RATING_PROMPT;
  }

  public String buildReviewPrompt() {
    return REVIEW_PROMPT;
  }

  public String buildReturnPrompt() {
    return RETURN_PROMPT;
  }
}
