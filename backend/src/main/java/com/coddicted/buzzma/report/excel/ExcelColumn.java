package com.coddicted.buzzma.report.excel;

import java.util.function.Function;

/** Declares one report column: its header label and how to read its value off a row of type T. */
public record ExcelColumn<T>(String header, Function<T, Object> valueExtractor) {}
