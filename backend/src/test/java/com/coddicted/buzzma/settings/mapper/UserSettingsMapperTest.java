package com.coddicted.buzzma.settings.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.coddicted.buzzma.settings.dto.UserSettingsFlagDto;
import com.coddicted.buzzma.settings.entity.Settings;
import com.coddicted.buzzma.settings.entity.UserSettingsFlag;
import java.util.List;
import org.junit.jupiter.api.Test;

class UserSettingsMapperTest {

  private final UserSettingsMapper mapper = new UserSettingsMapperImpl();

  @Test
  void testToUserSettingsFlagsIncludesAllFlagsInDefinedOrder() {
    final Settings settings = Settings.builder().build();

    final List<UserSettingsFlagDto> flags = this.mapper.toUserSettingsFlags(settings);

    assertEquals(
        List.of(UserSettingsFlag.values()),
        flags.stream().map(UserSettingsFlagDto::getFlag).toList());
  }

  @Test
  void testToUserSettingsFlagsReflectsEnabledState() {
    final Settings settings =
        Settings.builder().myPaymentsTabEnabled(true).userPayoutsTabEnabled(false).build();

    final List<UserSettingsFlagDto> flags = this.mapper.toUserSettingsFlags(settings);

    final UserSettingsFlagDto myPayments =
        flags.stream()
            .filter(dto -> dto.getFlag() == UserSettingsFlag.MY_PAYMENTS_TAB_ENABLED)
            .findFirst()
            .orElseThrow();
    assertTrue(myPayments.isEnabled());
    assertEquals("My Payments", myPayments.getDisplayName());

    final UserSettingsFlagDto userPayouts =
        flags.stream()
            .filter(dto -> dto.getFlag() == UserSettingsFlag.USER_PAYOUTS_TAB_ENABLED)
            .findFirst()
            .orElseThrow();
    assertEquals(false, userPayouts.isEnabled());
  }
}
