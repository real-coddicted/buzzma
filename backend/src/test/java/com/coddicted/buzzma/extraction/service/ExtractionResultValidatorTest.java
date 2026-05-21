package com.coddicted.buzzma.extraction.service;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.coddicted.buzzma.extraction.entity.ExtractionResult;
import com.coddicted.buzzma.extraction.entity.ValidationError;
import com.coddicted.buzzma.shared.enums.Platform;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ExtractionResultValidatorTest {

  private ExtractionResultValidator validator;

  @BeforeEach
  void setUp() {
    this.validator = new ExtractionResultValidator();
  }

  @Test
  void testValidateWhenValid() {
    final ExtractionResult input =
        ExtractionResult.builder()
            .platform(Platform.PLATFORM_AMAZON)
            .orderId("171-5279451-9793313")
            .orderDate("2026-04-19")
            .productName(
                "SIMMON AUTOHUB Car Gear Shift Knob for EcoSport 2025 Year All Models of Ford Car 5 Speed Manual Transmission Gear Shift Knob")
            .sellerName("Simmon crafts")
            .amount(BigDecimal.valueOf(1004.00))
            .orderedBy("Anupam Jain")
            .build();

    final List<ValidationError> errors = this.validator.validate(input);

    // The provided JSON should be accepted by the validator -> no errors
    assertTrue(errors.isEmpty(), "Expected no validation errors for a valid extraction result");
  }

  @Test
  void testValidateWhenMissingPlatformAndOrderId() {
    final ExtractionResult input =
        ExtractionResult.builder()
            .platform(null)
            .orderId("")
            .orderDate("2026-04-19")
            .productName("Product")
            .amount(BigDecimal.valueOf(10))
            .orderedBy("Customer")
            .build();

    final List<ValidationError> errors = this.validator.validate(input);

    // Expect at least a platform and orderId error
    assertTrue(
        errors.stream().anyMatch(e -> "platform".equals(e.getField())),
        "Expected a platform validation error");
    assertTrue(
        errors.stream().anyMatch(e -> "orderId".equals(e.getField())),
        "Expected an orderId validation error");
  }

  @Test
  void testValidateWhenInvalidOrderDateAndAmount() {
    final ExtractionResult input =
        ExtractionResult.builder()
            .platform(Platform.PLATFORM_FLIPKART)
            .orderId("OD123456789012")
            .orderDate("19-04-2026")
            .productName("Product")
            .amount(BigDecimal.ZERO)
            .orderedBy("Customer")
            .build();

    final List<ValidationError> errors = this.validator.validate(input);

    assertTrue(
        errors.stream().anyMatch(e -> "orderDate".equals(e.getField())),
        "Expected an orderDate validation error for malformed date");
    assertTrue(
        errors.stream().anyMatch(e -> "amount".equals(e.getField())),
        "Expected an amount validation error for non-positive value");
  }
}
