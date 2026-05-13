package com.coddicted.buzzma.storage.dto;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class FileUploadResponseDto {
  String storageKey;
}
