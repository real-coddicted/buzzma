package com.coddicted.buzzma.claim.step;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.coddicted.buzzma.claim.scorer.OrderScreenshotScorer;
import com.coddicted.buzzma.extraction.entity.ValidationError;
import com.coddicted.buzzma.extraction.service.GeminiExtractionPromptBuilder;
import com.coddicted.buzzma.shared.constants.BuzzmahConstants;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrderStepDefinitionTest {

  @Mock private GeminiExtractionPromptBuilder mockPromptBuilder;
  @Mock private OrderScreenshotScorer mockScorer;

  private OrderStepDefinition stepDefinition;

  @BeforeEach
  void setUp() {
    this.stepDefinition = new OrderStepDefinition(this.mockPromptBuilder, this.mockScorer);
  }

  private Map<String, String> fields(final String... kv) {
    final Map<String, String> map = new HashMap<>();
    for (int i = 0; i < kv.length; i += 2) {
      map.put(kv[i], kv[i + 1]);
    }
    return map;
  }

  @Test
  void testValidateWhenValid() {
    final Map<String, String> extracted =
        fields(
            BuzzmahConstants.PLATFORM, "PLATFORM_AMAZON",
            BuzzmahConstants.ORDER_ID, "171-5279451-9793313",
            BuzzmahConstants.ORDER_DATE, "2026-04-19",
            BuzzmahConstants.PRODUCT_NAME, "Car Gear Shift Knob",
            BuzzmahConstants.AMOUNT, "1004.00",
            BuzzmahConstants.ORDERED_BY, "Anupam Jain");

    final List<ValidationError> errors = this.stepDefinition.validate(extracted);

    assertTrue(errors.isEmpty(), "Expected no validation errors for a valid extraction result");
  }

  @Test
  void testValidateWhenMissingPlatformAndOrderId() {
    final Map<String, String> extracted =
        fields(
            BuzzmahConstants.ORDER_DATE, "2026-04-19",
            BuzzmahConstants.PRODUCT_NAME, "Product",
            BuzzmahConstants.AMOUNT, "10",
            BuzzmahConstants.ORDERED_BY, "Customer");

    final List<ValidationError> errors = this.stepDefinition.validate(extracted);

    assertTrue(
        errors.stream().anyMatch(e -> BuzzmahConstants.PLATFORM.equals(e.getField())),
        "Expected a platform validation error");
    assertTrue(
        errors.stream().anyMatch(e -> BuzzmahConstants.ORDER_ID.equals(e.getField())),
        "Expected an orderId validation error");
  }

  @Test
  void testValidateWhenInvalidOrderDateAndAmount() {
    final Map<String, String> extracted =
        fields(
            BuzzmahConstants.PLATFORM, "PLATFORM_FLIPKART",
            BuzzmahConstants.ORDER_ID, "OD123456789012",
            BuzzmahConstants.ORDER_DATE, "19-04-2026",
            BuzzmahConstants.PRODUCT_NAME, "Product",
            BuzzmahConstants.AMOUNT, "0",
            BuzzmahConstants.ORDERED_BY, "Customer");

    final List<ValidationError> errors = this.stepDefinition.validate(extracted);

    assertTrue(
        errors.stream().anyMatch(e -> BuzzmahConstants.ORDER_DATE.equals(e.getField())),
        "Expected an orderDate validation error for malformed date");
    assertTrue(
        errors.stream().anyMatch(e -> BuzzmahConstants.AMOUNT.equals(e.getField())),
        "Expected an amount validation error for non-positive value");
  }

  @Test
  void testValidateWhenMeeshoOrderIdIsValid() {
    final Map<String, String> extracted =
        fields(
            BuzzmahConstants.PLATFORM, "PLATFORM_MEESHO",
            BuzzmahConstants.ORDER_ID, "316168282897699392",
            BuzzmahConstants.ORDER_DATE, "2026-04-19",
            BuzzmahConstants.PRODUCT_NAME, "Product",
            BuzzmahConstants.AMOUNT, "499",
            BuzzmahConstants.ORDERED_BY, "Customer");

    final List<ValidationError> errors = this.stepDefinition.validate(extracted);

    assertTrue(
        errors.stream().noneMatch(e -> BuzzmahConstants.ORDER_ID.equals(e.getField())),
        "Expected no orderId validation error for a valid 18-digit Meesho order ID");
  }

  @Test
  void testValidateWhenMeeshoOrderIdIsInvalid() {
    final Map<String, String> extracted =
        fields(
            BuzzmahConstants.PLATFORM, "PLATFORM_MEESHO",
            BuzzmahConstants.ORDER_ID, "12345",
            BuzzmahConstants.ORDER_DATE, "2026-04-19",
            BuzzmahConstants.PRODUCT_NAME, "Product",
            BuzzmahConstants.AMOUNT, "499",
            BuzzmahConstants.ORDERED_BY, "Customer");

    final List<ValidationError> errors = this.stepDefinition.validate(extracted);

    assertTrue(
        errors.stream().anyMatch(e -> BuzzmahConstants.ORDER_ID.equals(e.getField())),
        "Expected an orderId validation error for a non-18-digit Meesho order ID");
  }
}
