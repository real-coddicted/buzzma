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

  @Mapping(source = "dashboardTabEnabled", target = "isDashboardTabEnabled")
  @Mapping(source = "campaignsTabEnabled", target = "isCampaignsTabEnabled")
  @Mapping(source = "assignmentsTabEnabled", target = "isAssignmentsTabEnabled")
  @Mapping(source = "connectionsTabEnabled", target = "isConnectionsTabEnabled")
  @Mapping(source = "dealTabEnabled", target = "isDealTabEnabled")
  @Mapping(source = "ticketsTabEnabled", target = "isTicketsTabEnabled")
  @Mapping(source = "feedbackTabEnabled", target = "isFeedbackTabEnabled")
  @Mapping(source = "settingsTabEnabled", target = "isSettingsTabEnabled")
  Settings toSettings(UserSettingsDto dto);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "userId", ignore = true)
  @Mapping(target = "settings", ignore = true)
  @Mapping(target = "createdBy", ignore = true)
  @Mapping(target = "updatedBy", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  @Mapping(target = "isDeleted", ignore = true)
  UserSettings toEntity(UserSettingsDto dto);
}
