package com.coddicted.buzzma.shared.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.coddicted.buzzma.shared.common.CodeGenerator;
import com.coddicted.buzzma.shared.constants.WellKnownSequences;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

@ExtendWith(MockitoExtension.class)
class CodeGenerationServiceImplTest {

  private static final long SEQ_VALUE = 1000001L;
  private static final int DEFAULT_LENGTH = 8;

  @Mock private JdbcTemplate mockJdbcTemplate;

  private CodeGenerationServiceImpl codeGenerationService;

  @BeforeEach
  void setUp() {
    this.codeGenerationService = new CodeGenerationServiceImpl(this.mockJdbcTemplate);
  }

  @Test
  void testGenerateCodeFromSequence() {
    when(this.mockJdbcTemplate.queryForObject(
            "SELECT nextval(?)", Long.class, WellKnownSequences.CAMPAIGN))
        .thenReturn(SEQ_VALUE);

    final String result =
        this.codeGenerationService.generateCodeFromSequence(WellKnownSequences.CAMPAIGN);

    final String expected = CodeGenerator.INSTANCE.generateCode(SEQ_VALUE, DEFAULT_LENGTH);
    assertEquals(expected, result);
    verify(this.mockJdbcTemplate)
        .queryForObject("SELECT nextval(?)", Long.class, WellKnownSequences.CAMPAIGN);
  }
}
