package com.coddicted.buzzma.shared.util;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public final class DateTimeUtils {

  public static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.BASIC_ISO_DATE;

  public static final DateTimeFormatter TIMESTAMP_FORMAT =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneOffset.UTC);

  public static LocalDate toLocalDate(final int date) {
    return LocalDate.parse(String.valueOf(date), DATE_FORMAT);
  }

  public static int toIntDate(final LocalDate date) {
    return Integer.parseInt(date.format(DATE_FORMAT));
  }

  public static String formatTimestamp(final Instant instant) {
    return instant != null ? TIMESTAMP_FORMAT.format(instant) : null;
  }
}
