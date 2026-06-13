package com.coddicted.buzzma.shared.converter;

import com.coddicted.buzzma.extraction.entity.ScoredValue;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class ScoredValueConverter implements Converter<String, ScoredValue> {

  private final ObjectMapper objectMapper;

  public ScoredValueConverter(final ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  @Override
  public ScoredValue convert(final String source) {
    try {
      return this.objectMapper.readValue(source, ScoredValue.class);
    } catch (final JsonProcessingException e) {
      throw new IllegalArgumentException("Invalid ScoredValue JSON: " + source, e);
    }
  }
}
