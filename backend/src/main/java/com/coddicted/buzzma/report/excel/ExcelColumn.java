package com.coddicted.buzzma.report.excel;

import java.util.List;
import java.util.function.Function;

/**
 * Declares one report column: its header label, how to read its value off a row of type T, and
 * optionally a fixed set of options to render as a dropdown on the column's data cells.
 */
public record ExcelColumn<T>(
    String header, Function<T, Object> valueExtractor, List<String> dropdownOptions) {

  public ExcelColumn(final String header, final Function<T, Object> valueExtractor) {
    this(header, valueExtractor, List.of());
  }
}
