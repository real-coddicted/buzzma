package com.coddicted.buzzma.extraction.service;

import com.coddicted.buzzma.extraction.entity.ExtractionResult;
import com.coddicted.buzzma.extraction.entity.ValidationError;
import com.coddicted.buzzma.shared.enums.Platform;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

@Component
public class ExtractionResultValidator {

  private static final Pattern AMAZON_ORDER_ID = Pattern.compile("^\\d{3}-\\d{7}-\\d{7}$");
  private static final Pattern FLIPKART_ORDER_ID = Pattern.compile("^OD\\d{12,18}$");
  private static final Pattern MYNTRA_ORDER_ID = Pattern.compile("^\\d{8,20}$");

  public List<ValidationError> validate(final ExtractionResult result) {
    final List<ValidationError> errors = new ArrayList<>();

    if (result.getPlatform() == null) {
      errors.add(error("platform", "Platform is required"));
    }

    if (isBlank(result.getOrderId())) {
      errors.add(error("orderId", "Order ID is required"));
    } else if (result.getPlatform() != null) {
      validateOrderIdFormat(result.getPlatform(), result.getOrderId(), errors);
    }

    if (isBlank(result.getOrderDate())) {
      errors.add(error("orderDate", "Order date is required"));
    } else {
      try {
        LocalDate.parse(result.getOrderDate());
      } catch (DateTimeParseException e) {
        errors.add(error("orderDate", "Order date must be in YYYY-MM-DD format"));
      }
    }

    if (isBlank(result.getProductName())) {
      errors.add(error("productName", "Product name is required"));
    }

    if (result.getAmount() == null || result.getAmount().signum() <= 0) {
      errors.add(error("amount", "Amount must be greater than 0"));
    }

    if (isBlank(result.getOrderedBy())) {
      errors.add(error("orderedBy", "Customer name is required"));
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
          default -> true;
        };
    if (!valid) {
      errors.add(
          error(
              "orderId",
              "Order ID " + orderId + " format is invalid for platform " + platform.name()));
    }
  }

  private ValidationError error(final String field, final String message) {
    return ValidationError.builder().field(field).message(message).build();
  }

  private boolean isBlank(final String s) {
    return s == null || s.isBlank();
  }
}
