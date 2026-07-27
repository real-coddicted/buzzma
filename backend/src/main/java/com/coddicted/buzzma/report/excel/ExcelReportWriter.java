package com.coddicted.buzzma.report.excel;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataValidation;
import org.apache.poi.ss.usermodel.DataValidationConstraint;
import org.apache.poi.ss.usermodel.DataValidationHelper;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.stereotype.Component;

/**
 * Renders a list of rows into an .xlsx workbook given a column definition, independent of what the
 * rows represent. Report-specific code only needs to supply {@link ExcelColumn}s and data.
 */
@Component
public class ExcelReportWriter {

  public <T> byte[] write(
      final String sheetName, final List<ExcelColumn<T>> columns, final List<T> rows) {
    final SXSSFWorkbook workbook = new SXSSFWorkbook();
    try {
      final Sheet sheet = workbook.createSheet(sheetName);
      writeHeaderRow(sheet, columns, headerStyle(workbook));
      writeDataRows(sheet, columns, rows);
      writeDropdowns(sheet, columns, rows.size());
      return toBytes(workbook);
    } catch (final IOException e) {
      throw new UncheckedIOException("Failed to generate Excel report", e);
    } finally {
      workbook.dispose();
      closeQuietly(workbook);
    }
  }

  private <T> void writeHeaderRow(
      final Sheet sheet, final List<ExcelColumn<T>> columns, final CellStyle headerStyle) {
    final Row header = sheet.createRow(0);
    for (int i = 0; i < columns.size(); i++) {
      final Cell cell = header.createCell(i);
      cell.setCellValue(columns.get(i).header());
      cell.setCellStyle(headerStyle);
    }
  }

  private <T> void writeDataRows(
      final Sheet sheet, final List<ExcelColumn<T>> columns, final List<T> rows) {
    for (int r = 0; r < rows.size(); r++) {
      final Row row = sheet.createRow(r + 1);
      final T rowData = rows.get(r);
      for (int c = 0; c < columns.size(); c++) {
        setCellValue(row.createCell(c), columns.get(c).valueExtractor().apply(rowData));
      }
    }
  }

  private <T> void writeDropdowns(
      final Sheet sheet, final List<ExcelColumn<T>> columns, final int rowCount) {
    if (rowCount == 0) {
      return;
    }
    final DataValidationHelper helper = sheet.getDataValidationHelper();
    for (int c = 0; c < columns.size(); c++) {
      final List<String> options = columns.get(c).dropdownOptions();
      if (options.isEmpty()) {
        continue;
      }
      final DataValidationConstraint constraint =
          helper.createExplicitListConstraint(options.toArray(new String[0]));
      final CellRangeAddressList range = new CellRangeAddressList(1, rowCount, c, c);
      final DataValidation validation = helper.createValidation(constraint, range);
      validation.setSuppressDropDownArrow(true);
      sheet.addValidationData(validation);
    }
  }

  private void setCellValue(final Cell cell, final Object value) {
    if (value == null) {
      cell.setBlank();
    } else if (value instanceof final Number number) {
      cell.setCellValue(number.doubleValue());
    } else if (value instanceof final Boolean bool) {
      cell.setCellValue(bool);
    } else {
      cell.setCellValue(value.toString());
    }
  }

  private CellStyle headerStyle(final SXSSFWorkbook workbook) {
    final Font boldFont = workbook.createFont();
    boldFont.setBold(true);
    boldFont.setColor(IndexedColors.WHITE.getIndex());
    final CellStyle style = workbook.createCellStyle();
    style.setFont(boldFont);
    style.setFillForegroundColor(IndexedColors.BLUE.getIndex());
    style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
    return style;
  }

  private byte[] toBytes(final SXSSFWorkbook workbook) throws IOException {
    final ByteArrayOutputStream out = new ByteArrayOutputStream();
    workbook.write(out);
    return out.toByteArray();
  }

  private void closeQuietly(final SXSSFWorkbook workbook) {
    try {
      workbook.close();
    } catch (final IOException ignored) {
      // workbook is already fully written to the byte array; closing failure is not actionable
    }
  }
}
