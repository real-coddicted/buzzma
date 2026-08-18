package com.coddicted.buzzma.shared.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigInteger;
import org.junit.jupiter.api.Test;

class CurrencyUtilsTest {

  @Test
  void formatPaise_wholeRupees_formatsWithTwoDecimalsAndIndianGrouping() {
    assertEquals("₹1,00,000.00", CurrencyUtils.formatPaise(BigInteger.valueOf(10_000_000)));
  }

  @Test
  void formatPaise_fractionalRupees_formatsWithTwoDecimals() {
    assertEquals("₹123.46", CurrencyUtils.formatPaise(BigInteger.valueOf(12346)));
  }

  @Test
  void formatPaise_zero_formatsAsZero() {
    assertEquals("₹0.00", CurrencyUtils.formatPaise(BigInteger.ZERO));
  }

  @Test
  void formatPaise_crores_groupsInIndianStyle() {
    assertEquals("₹1,23,45,678.00", CurrencyUtils.formatPaise(BigInteger.valueOf(1_234_567_800L)));
  }
}
