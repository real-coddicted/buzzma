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

  @Mapping(source = "settings.dashboardTabEnabled", target = "dashboardTabEnabled")
  @Mapping(source = "settings.campaignsTabEnabled", target = "campaignsTabEnabled")
  @Mapping(source = "settings.assignmentsTabEnabled", target = "assignmentsTabEnabled")
  @Mapping(source = "settings.connectionsTabEnabled", target = "connectionsTabEnabled")
  @Mapping(source = "settings.dealTabEnabled", target = "dealTabEnabled")
  @Mapping(source = "settings.claimReviewEnabled", target = "claimReviewEnabled")
  @Mapping(source = "settings.ticketsTabEnabled", target = "ticketsTabEnabled")
  @Mapping(source = "settings.feedbackTabEnabled", target = "feedbackTabEnabled")
  @Mapping(source = "settings.settingsTabEnabled", target = "settingsTabEnabled")
  @Mapping(source = "settings.usersTabEnabled", target = "usersTabEnabled")
  UserSettingsDto toUserSettingsDto(UserSettings userSettings);

  @Mapping(source = "dashboardTabEnabled", target = "dashboardTabEnabled")
  @Mapping(source = "campaignsTabEnabled", target = "campaignsTabEnabled")
  @Mapping(source = "assignmentsTabEnabled", target = "assignmentsTabEnabled")
  @Mapping(source = "connectionsTabEnabled", target = "connectionsTabEnabled")
  @Mapping(source = "dealTabEnabled", target = "dealTabEnabled")
  @Mapping(source = "claimReviewEnabled", target = "claimReviewEnabled")
  @Mapping(source = "ticketsTabEnabled", target = "ticketsTabEnabled")
  @Mapping(source = "feedbackTabEnabled", target = "feedbackTabEnabled")
  @Mapping(source = "settingsTabEnabled", target = "settingsTabEnabled")
  @Mapping(source = "usersTabEnabled", target = "usersTabEnabled")
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
