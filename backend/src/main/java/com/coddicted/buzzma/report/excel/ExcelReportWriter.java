package com.coddicted.buzzma.report.excel;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
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

  private void setCellValue(final Cell cell, final Object value) {
    if (value == null) {
      cell.setBlank();
    } else if (value instanceof Number number) {
      cell.setCellValue(number.doubleValue());
    } else if (value instanceof Boolean bool) {
      cell.setCellValue(bool);
    } else {
      cell.setCellValue(value.toString());
    }
  }

  private CellStyle headerStyle(final SXSSFWorkbook workbook) {
    final Font boldFont = workbook.createFont();
    boldFont.setBold(true);
    final CellStyle style = workbook.createCellStyle();
    style.setFont(boldFont);
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
