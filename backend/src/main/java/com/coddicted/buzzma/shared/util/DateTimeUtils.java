package com.coddicted.buzzma.shared.util;

import com.coddicted.buzzma.shared.exception.BusinessRuleViolationException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public final class DateTimeUtils {

  public static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.BASIC_ISO_DATE;

  private static final ZoneId ASIA_KOLKATA = ZoneId.of("Asia/Kolkata");

  public static final DateTimeFormatter TIMESTAMP_FORMAT =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneOffset.UTC);

  public static LocalDate toLocalDate(final int date) {
    return LocalDate.parse(String.valueOf(date), DATE_FORMAT);
  }

  public static int toIntDate(final LocalDate date) {
    return Integer.parseInt(date.format(DATE_FORMAT));
  }

  public static int getAsianTodayDate() {
    return toIntDate(LocalDate.now(ASIA_KOLKATA));
  }

  public static void validateEndDateNotInPast(final Integer endDate) {
    if (endDate != null && endDate < getAsianTodayDate()) {
      throw new BusinessRuleViolationException("Campaign end date cannot be in the past");
    }
  }

  public static String formatTimestamp(final Instant instant) {
    return instant != null ? TIMESTAMP_FORMAT.format(instant) : null;
  }
}
