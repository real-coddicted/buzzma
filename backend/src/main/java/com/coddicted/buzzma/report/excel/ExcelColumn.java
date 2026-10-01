package com.coddicted.buzzma.report.excel;

import java.util.List;
import java.util.function.Function;

/**
 * Declares one report column: its header label, how to read its value off a row of type T,
 * optionally a fixed set of options to render as a dropdown on the column's data cells, and whether
 * its value should be written as a clickable hyperlink cell.
 */
public record ExcelColumn<T>(
    String header,
    Function<T, Object> valueExtractor,
    List<String> dropdownOptions,
    boolean hyperlink,
    String hyperlinkLabel) {

  public ExcelColumn(final String header, final Function<T, Object> valueExtractor) {
    this(header, valueExtractor, List.of(), false, null);
  }

  public ExcelColumn(
      final String header,
      final Function<T, Object> valueExtractor,
      final List<String> dropdownOptions) {
    this(header, valueExtractor, dropdownOptions, false, null);
  }

  /** A column whose non-null values are rendered as clickable Excel hyperlinks. */
  public static <T> ExcelColumn<T> hyperlink(
      final String header, final Function<T, Object> valueExtractor) {
    return new ExcelColumn<>(header, valueExtractor, List.of(), true, null);
  }

  /** A hyperlink column that displays a fixed label instead of the URL. */
  public static <T> ExcelColumn<T> hyperlink(
      final String header, final Function<T, Object> valueExtractor, final String label) {
    return new ExcelColumn<>(header, valueExtractor, List.of(), true, label);
  }
}
