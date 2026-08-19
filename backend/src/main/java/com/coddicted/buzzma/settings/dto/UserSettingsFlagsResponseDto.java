package com.coddicted.buzzma.settings.dto;

import java.util.List;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class UserSettingsFlagsResponseDto {
  List<UserSettingsFlagDto> flags;
}
