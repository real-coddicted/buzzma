package com.coddicted.buzzma.settings.mapper;

import com.coddicted.buzzma.settings.dto.UserSettingsDto;
import com.coddicted.buzzma.settings.entity.Settings;
import com.coddicted.buzzma.settings.entity.UserSettings;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface UserSettingsMapper {

  @Mapping(source = "settings.dashboardTabEnabled", target = "isDashboardTabEnabled")
  @Mapping(source = "settings.campaignsTabEnabled", target = "isCampaignsTabEnabled")
  @Mapping(source = "settings.assignmentsTabEnabled", target = "isAssignmentsTabEnabled")
  @Mapping(source = "settings.connectionsTabEnabled", target = "isConnectionsTabEnabled")
  @Mapping(source = "settings.dealTabEnabled", target = "isDealTabEnabled")
  @Mapping(source = "settings.ticketsTabEnabled", target = "isTicketsTabEnabled")
  @Mapping(source = "settings.feedbackTabEnabled", target = "isFeedbackTabEnabled")
  @Mapping(source = "settings.settingsTabEnabled", target = "isSettingsTabEnabled")
  UserSettingsDto toUserSettingsDto(UserSettings userSettings);

  Settings toSettings(UserSettingsDto dto);

  UserSettings toEntity(UserSettingsDto dto);
}
