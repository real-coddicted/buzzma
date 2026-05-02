package com.coddicted.buzzma.shared.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public final class DateTimeUtils {

  public static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.BASIC_ISO_DATE;

  public static LocalDate toLocalDate(final int date) {
    return LocalDate.parse(String.valueOf(date), DATE_FORMAT);
  }
}
