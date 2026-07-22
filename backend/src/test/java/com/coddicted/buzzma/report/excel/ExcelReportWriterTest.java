package com.coddicted.buzzma.report.excel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.util.List;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.junit.jupiter.api.Test;

class ExcelReportWriterTest {

  private record Person(String name, int age) {}

  private final ExcelReportWriter writer = new ExcelReportWriter();

  @Test
  void testWriteProducesHeaderAndDataRows() throws Exception {
    final List<ExcelColumn<Person>> columns =
        List.of(new ExcelColumn<>("Name", Person::name), new ExcelColumn<>("Age", Person::age));
    final List<Person> rows = List.of(new Person("Alice", 30), new Person("Bob", 25));

    final byte[] bytes = writer.write("People", columns, rows);

    try (Workbook workbook = WorkbookFactory.create(new ByteArrayInputStream(bytes))) {
      final Sheet sheet = workbook.getSheet("People");
      final Row header = sheet.getRow(0);
      assertEquals("Name", header.getCell(0).getStringCellValue());
      assertEquals("Age", header.getCell(1).getStringCellValue());

      final Row firstRow = sheet.getRow(1);
      assertEquals("Alice", firstRow.getCell(0).getStringCellValue());
      assertEquals(30.0, firstRow.getCell(1).getNumericCellValue());

      final Row secondRow = sheet.getRow(2);
      assertEquals("Bob", secondRow.getCell(0).getStringCellValue());
    }
  }

  @Test
  void testWriteHandlesNullValuesAsBlankCells() throws Exception {
    final List<ExcelColumn<Person>> columns = List.of(new ExcelColumn<>("Name", p -> null));
    final byte[] bytes = writer.write("People", columns, List.of(new Person("Alice", 30)));

    try (Workbook workbook = WorkbookFactory.create(new ByteArrayInputStream(bytes))) {
      final Row dataRow = workbook.getSheet("People").getRow(1);
      assertTrue(dataRow.getCell(0).getStringCellValue().isEmpty());
    }
  }
}
