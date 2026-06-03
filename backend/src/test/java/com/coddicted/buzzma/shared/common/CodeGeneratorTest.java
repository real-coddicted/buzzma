package com.coddicted.buzzma.shared.common;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class CodeGeneratorTest {

  private static final String SAFE_CHARS = "X3NKBHP2MZTJ4RCFQ8YVGW5ADSE76UL9";

  @Test
  void testGenerateCodeLength() {
    final String code = CodeGenerator.INSTANCE.generateCode(1000001L, 8);
    // 8 chars + 1 hyphen
    assertEquals(9, code.length());
  }

  @Test
  void testGenerateCodeHyphenAtPosition4() {
    final String code = CodeGenerator.INSTANCE.generateCode(1000001L, 8);
    assertEquals('-', code.charAt(4));
  }

  @Test
  void testGenerateCodeOnlyUsesCharsFromSafeChars() {
    final String code = CodeGenerator.INSTANCE.generateCode(1000001L, 8);
    for (final char c : code.toCharArray()) {
      if (c != '-') {
        assertTrue(SAFE_CHARS.indexOf(c) >= 0, "Unexpected char: " + c);
      }
    }
  }

  @Test
  void testGenerateCodeIsDeterministic() {
    final String first = CodeGenerator.INSTANCE.generateCode(1000001L, 8);
    final String second = CodeGenerator.INSTANCE.generateCode(1000001L, 8);
    assertEquals(first, second);
  }

  @Test
  void testGenerateCodeDifferentValuesProduceDifferentCodes() {
    final String code1 = CodeGenerator.INSTANCE.generateCode(1000001L, 8);
    final String code2 = CodeGenerator.INSTANCE.generateCode(1000002L, 8);
    assertNotEquals(code1, code2);
  }

  @Test
  void testGenerateCodeMinLength() {
    final String code = CodeGenerator.INSTANCE.generateCode(1000001L, 4);
    // single group — no hyphen
    assertEquals(4, code.length());
    assertFalse(code.contains("-"));
  }

  @Test
  void testGenerateCodeMaxLength() {
    final String code = CodeGenerator.INSTANCE.generateCode(1000001L, 10);
    // 10 chars + 2 hyphens → XXXX-XXXX-XX
    assertEquals(12, code.length());
    assertEquals('-', code.charAt(4));
    assertEquals('-', code.charAt(9));
  }

  @Test
  void testGenerateCodeHyphenEvery4Chars() {
    final String code = CodeGenerator.INSTANCE.generateCode(1000001L, 10);
    // verify the pattern: 4 chars, hyphen, 4 chars, hyphen, 2 chars
    final String[] parts = code.split("-");
    assertEquals(3, parts.length);
    assertEquals(4, parts[0].length());
    assertEquals(4, parts[1].length());
    assertEquals(2, parts[2].length());
  }

  @Test
  void testGenerateCodeLengthBelowMinThrows() {
    final IllegalArgumentException ex =
        assertThrows(
            IllegalArgumentException.class, () -> CodeGenerator.INSTANCE.generateCode(1000001L, 3));
    assertEquals("length must be between 4 and 10, got 3", ex.getMessage());
  }

  @Test
  void testGenerateCodeLengthAboveMaxThrows() {
    final IllegalArgumentException ex =
        assertThrows(
            IllegalArgumentException.class,
            () -> CodeGenerator.INSTANCE.generateCode(1000001L, 11));
    assertEquals("length must be between 4 and 10, got 11", ex.getMessage());
  }
}
