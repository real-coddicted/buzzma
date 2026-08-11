package com.coddicted.buzzma.terms.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class TermsDto {
  String content;
  String version;
}
