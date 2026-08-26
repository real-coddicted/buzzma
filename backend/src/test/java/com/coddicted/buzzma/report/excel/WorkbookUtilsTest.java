package com.coddicted.buzzma.report.excel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

class WorkbookUtilsTest {

  private static final int COLUMN_COUNT = 16;

  @Test
  void countDataRowsSkipsRowsWhereEveryExpectedCellIsBlank() throws Exception {
    try (XSSFWorkbook workbook = new XSSFWorkbook()) {
      final Sheet sheet = workbook.createSheet("Sheet1");

      final Row dataRow = sheet.createRow(1);
      dataRow.createCell(0).setCellValue("Campaign");

      // Simulates rows left behind by Excel with present-but-blank cells (e.g. from data
      // validation formatting applied to a wider range than the actual data) - no real values.
      final Row blankRow = sheet.createRow(2);
      for (int c = 0; c < COLUMN_COUNT; c++) {
        blankRow.createCell(c);
      }

      assertEquals(1, WorkbookUtils.countDataRows(sheet, COLUMN_COUNT));
    }
  }

  @Test
  void isDataRowTrueIfAnyExpectedCellIsNonBlank() throws Exception {
    try (XSSFWorkbook workbook = new XSSFWorkbook()) {
      final Sheet sheet = workbook.createSheet("Sheet1");

      final Row withValue = sheet.createRow(0);
      withValue.createCell(0).setCellValue("Campaign");
      assertTrue(WorkbookUtils.isDataRow(withValue, COLUMN_COUNT));

      final Row allBlank = sheet.createRow(1);
      for (int c = 0; c < COLUMN_COUNT; c++) {
        allBlank.createCell(c);
      }
      assertFalse(WorkbookUtils.isDataRow(allBlank, COLUMN_COUNT));

      assertFalse(WorkbookUtils.isDataRow(null, COLUMN_COUNT));
    }
  }
}
