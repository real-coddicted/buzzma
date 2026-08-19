package com.coddicted.buzzma.settings.dto;

import com.coddicted.buzzma.settings.entity.UserSettingsFlag;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class UserSettingsFlagDto {
  UserSettingsFlag flag;
  boolean enabled;
  String displayName;
}
