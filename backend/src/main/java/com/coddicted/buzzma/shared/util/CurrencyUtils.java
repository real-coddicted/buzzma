package com.coddicted.buzzma.shared.util;

import java.math.BigInteger;

public final class CurrencyUtils {

  private static final BigInteger HUNDRED = BigInteger.valueOf(100);

  private CurrencyUtils() {}

  public static String formatPaise(final BigInteger amountPaise) {
    final BigInteger[] rupeesAndPaise = amountPaise.divideAndRemainder(HUNDRED);
    return "₹"
        + groupIndian(rupeesAndPaise[0].toString())
        + "."
        + String.format("%02d", rupeesAndPaise[1].intValue());
  }

  /** Groups digits Indian-style: last 3 digits together, then pairs of 2 (e.g. 1,00,000). */
  private static String groupIndian(final String digits) {
    if (digits.length() <= 3) {
      return digits;
    }
    final String lastThree = digits.substring(digits.length() - 3);
    final String rest = digits.substring(0, digits.length() - 3);
    final StringBuilder grouped = new StringBuilder();
    int end = rest.length();
    while (end > 2) {
      grouped.insert(0, "," + rest.substring(end - 2, end));
      end -= 2;
    }
    grouped.insert(0, rest.substring(0, end));
    return grouped + "," + lastThree;
  }
}
