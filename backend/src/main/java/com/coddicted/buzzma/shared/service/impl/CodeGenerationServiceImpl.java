package com.coddicted.buzzma.shared.service.impl;

import com.coddicted.buzzma.shared.common.CodeGenerator;
import com.coddicted.buzzma.shared.constants.WellKnownSequences;
import com.coddicted.buzzma.shared.service.CodeGenerationService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class CodeGenerationServiceImpl implements CodeGenerationService {

  private static final int DEFAULT_LENGTH = 8;

  private final JdbcTemplate jdbcTemplate;

  public CodeGenerationServiceImpl(final JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  @Override
  public String generateCodeFromSequence(final WellKnownSequences sequence) {
    final long value =
        this.jdbcTemplate.queryForObject(
            "SELECT nextval(?)", Long.class, sequence.getSequenceName());
    final String code = CodeGenerator.INSTANCE.generateCode(value, DEFAULT_LENGTH);
    return sequence.getPrefix() + "-" + code;
  }
}
