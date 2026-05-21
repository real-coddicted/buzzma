package com.coddicted.buzzma.extraction.service;

import org.springframework.stereotype.Component;

@Component
public class GeminiExtractionPromptBuilder {

  private static final String PROMPT =
      """
      You are an order-data extractor. Analyze the provided e-commerce order screenshot and \
      return ONLY valid JSON with no markdown fences, no extra text, and no explanation. \
      The JSON must match this exact schema:
      {
        "platform": "<PLATFORM_AMAZON|PLATFORM_FLIPKART|PLATFORM_MYNTRA|PLATFORM_NYKAA|null>",
        "orderId": "<order identifier string or null>",
        "orderDate": "<YYYY-MM-DD or null>",
        "productName": "<product name string or null>",
        "sellerName": "<seller or sold-by name string or null>",
        "amount": <total order amount as a number without currency symbol, or null>,
        "orderedBy": "<customer full name or null>"
      }
      Use null for any field that cannot be clearly determined from the image.""";

  public String build() {
    return PROMPT;
  }
}
