package com.coddicted.buzzma.configurator.converter;

import com.coddicted.buzzma.configurator.enums.ValueTypeEnum;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class ValueTypeConverter implements AttributeConverter<ValueTypeEnum, String> {

  @Override
  public String convertToDatabaseColumn(ValueTypeEnum attribute) {
    return attribute == null ? null : attribute.name().toLowerCase();
  }

  @Override
  public ValueTypeEnum convertToEntityAttribute(String dbData) {
    return dbData == null ? null : ValueTypeEnum.valueOf(dbData.toUpperCase());
  }
}
