package com.coddicted.buzzma.claim.step;

import com.coddicted.buzzma.campaign.entity.CampaignStepType;
import com.coddicted.buzzma.claim.scorer.ClaimScreenshotScorer;
import com.coddicted.buzzma.claim.scorer.OrderScreenshotScorer;
import com.coddicted.buzzma.extraction.entity.ValidationError;
import com.coddicted.buzzma.extraction.service.GeminiExtractionPromptBuilder;
import com.coddicted.buzzma.shared.constants.BuzzmahConstants;
import com.coddicted.buzzma.shared.enums.Platform;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

/** Order is the campaign's blocking first step, so extraction runs synchronously. */
@Component
public class OrderStepDefinition implements StepDefinition {

  private static final Pattern AMAZON_ORDER_ID = Pattern.compile("^\\d{3}-\\d{7}-\\d{7}$");
  private static final Pattern FLIPKART_ORDER_ID = Pattern.compile("^OD\\d{12,18}$");
  private static final Pattern MYNTRA_ORDER_ID = Pattern.compile("^\\d{8,20}$");
  private static final Pattern NYKAA_ORDER_ID = Pattern.compile("^NYK-\\d{9}-\\d{7}$");
  private static final Pattern MEESHO_ORDER_ID = Pattern.compile("^\\d{18}$");

  private final GeminiExtractionPromptBuilder promptBuilder;
  private final OrderScreenshotScorer scorer;

  public OrderStepDefinition(
      final GeminiExtractionPromptBuilder promptBuilder, final OrderScreenshotScorer scorer) {
    this.promptBuilder = promptBuilder;
    this.scorer = scorer;
  }

  @Override
  public CampaignStepType stepType() {
    return CampaignStepType.ORDER;
  }

  @Override
  public boolean mediaRequired() {
    return true;
  }

  @Override
  public MediaType mediaType() {
    return MediaType.SCREENSHOT;
  }

  @Override
  public Optional<String> extractionPrompt() {
    return Optional.of(this.promptBuilder.build());
  }

  @Override
  public List<ValidationError> validate(final Map<String, String> extractedFields) {
    final List<ValidationError> errors = new ArrayList<>();

    final String platformRaw = extractedFields.get(BuzzmahConstants.PLATFORM);
    final Platform platform = Platform.parse(platformRaw);
    if (platform == null) {
      errors.add(
          error(
              BuzzmahConstants.PLATFORM,
              isBlank(platformRaw) ? "Platform is required" : "Unknown platform: " + platformRaw));
    }

    final String orderId = extractedFields.get(BuzzmahConstants.ORDER_ID);
    if (isBlank(orderId)) {
      errors.add(error(BuzzmahConstants.ORDER_ID, "Order ID is required"));
    } else if (platform != null) {
      validateOrderIdFormat(platform, orderId, errors);
    }

    final String orderDate = extractedFields.get(BuzzmahConstants.ORDER_DATE);
    if (isBlank(orderDate)) {
      errors.add(error(BuzzmahConstants.ORDER_DATE, "Order date is required"));
    } else {
      try {
        LocalDate.parse(orderDate);
      } catch (final DateTimeParseException e) {
        errors.add(error(BuzzmahConstants.ORDER_DATE, "Order date must be in YYYY-MM-DD format"));
      }
    }

    if (isBlank(extractedFields.get(BuzzmahConstants.PRODUCT_NAME))) {
      errors.add(error(BuzzmahConstants.PRODUCT_NAME, "Product name is required"));
    }

    final String amountRaw = extractedFields.get(BuzzmahConstants.AMOUNT);
    if (!isPositiveAmount(amountRaw)) {
      errors.add(error(BuzzmahConstants.AMOUNT, "Amount must be greater than 0"));
    }

    if (isBlank(extractedFields.get(BuzzmahConstants.ORDERED_BY))) {
      errors.add(error(BuzzmahConstants.ORDERED_BY, "Customer name is required"));
    }

    return errors;
  }

  private void validateOrderIdFormat(
      final Platform platform, final String orderId, final List<ValidationError> errors) {
    final boolean valid =
        switch (platform) {
          case PLATFORM_AMAZON -> AMAZON_ORDER_ID.matcher(orderId).matches();
          case PLATFORM_FLIPKART -> FLIPKART_ORDER_ID.matcher(orderId).matches();
          case PLATFORM_MYNTRA -> MYNTRA_ORDER_ID.matcher(orderId).matches();
          case PLATFORM_NYKAA -> NYKAA_ORDER_ID.matcher(orderId).matches();
          case PLATFORM_MEESHO -> MEESHO_ORDER_ID.matcher(orderId).matches();
          default -> true;
        };
    if (!valid) {
      errors.add(
          error(
              BuzzmahConstants.ORDER_ID,
              "Order ID " + orderId + " format is invalid for platform " + platform.name()));
    }
  }

  private boolean isPositiveAmount(final String amountRaw) {
    if (isBlank(amountRaw)) {
      return false;
    }
    try {
      return new BigDecimal(amountRaw).signum() > 0;
    } catch (final NumberFormatException e) {
      return false;
    }
  }

  private ValidationError error(final String field, final String message) {
    return ValidationError.builder().field(field).message(message).build();
  }

  private boolean isBlank(final String s) {
    return s == null || s.isBlank();
  }

  @Override
  public boolean scoringRequired() {
    return false;
  }

  @Override
  public ClaimScreenshotScorer scorer() {
    return this.scorer;
  }

  /**
   * The buyer runs {@code /extraction/sync} to pre-fill these from the screenshot (aiExtracted),
   * then confirms or edits them before the actual claim-creation submission (userProvided) — both
   * contribute to the final value.
   */
  @Override
  public List<StepField> fields() {
    return List.of(
        new StepField("platform", true, true),
        new StepField("orderId", true, true),
        new StepField("orderDate", true, true),
        new StepField("productName", true, true),
        new StepField("sellerName", true, true),
        new StepField("amount", true, true),
        new StepField("orderedBy", true, true));
  }
}
