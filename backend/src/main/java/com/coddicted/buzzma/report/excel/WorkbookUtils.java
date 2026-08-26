package com.coddicted.buzzma.report.excel;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

public final class WorkbookUtils {

  private WorkbookUtils() {}

  public static byte[] readBytes(final MultipartFile file) {
    try {
      return file.getBytes();
    } catch (final IOException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Failed to read uploaded file");
    }
  }

  public static Sheet openFirstSheet(final byte[] bytes) {
    try {
      final Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(bytes));
      return workbook.getSheetAt(0);
    } catch (final IOException e) {
      throw new ResponseStatusException(
          HttpStatus.UNPROCESSABLE_ENTITY, "Unable to parse uploaded file as Excel workbook");
    }
  }

  public static int countDataRows(final Sheet sheet, final int columnCount) {
    int count = 0;
    for (int r = 1; r <= sheet.getLastRowNum(); r++) {
      if (isDataRow(sheet.getRow(r), columnCount)) {
        count++;
      }
    }
    return count;
  }

  /**
   * A worksheet row is empty only if every one of its expected columns is blank; this ignores cells
   * Excel may leave behind with formatting but no value (e.g. from data validation applied to a
   * wider range than the actual data), which would otherwise look non-empty via getLastCellNum().
   */
  public static boolean isDataRow(final Row row, final int columnCount) {
    for (int c = 0; c < columnCount; c++) {
      if (cellString(row, c) != null) {
        return true;
      }
    }
    return false;
  }

  public static String cellString(final Row row, final int col) {
    if (row == null) {
      return null;
    }
    final Cell cell = row.getCell(col);
    if (cell == null || cell.getCellType() == CellType.BLANK) {
      return null;
    }
    if (cell.getCellType() == CellType.NUMERIC) {
      final double val = cell.getNumericCellValue();
      return val == Math.floor(val) ? String.valueOf((long) val) : String.valueOf(val);
    }
    return cell.toString().trim();
  }

  public static void validateHeaders(final Sheet sheet, final List<String> expectedHeaders) {
    final Row headerRow = sheet.getRow(0);
    if (headerRow == null) {
      throw new ResponseStatusException(
          HttpStatus.UNPROCESSABLE_ENTITY, "Worksheet has no header row");
    }
    for (int i = 0; i < expectedHeaders.size(); i++) {
      final Cell cell = headerRow.getCell(i);
      final String actual = cell != null ? cell.getStringCellValue().trim() : "";
      if (!expectedHeaders.get(i).equals(actual)) {
        throw new ResponseStatusException(
            HttpStatus.UNPROCESSABLE_ENTITY,
            "Header mismatch at column "
                + (i + 1)
                + ": expected '"
                + expectedHeaders.get(i)
                + "', found '"
                + actual
                + "'");
      }
    }
  }
}
