package com.coddicted.buzzma.campaign.dto;

import com.coddicted.buzzma.campaign.entity.CampaignStepType;
import java.util.ArrayList;
import java.util.List;

public record CampaignStepDto(String type, String label, int stepOrder) {

  public static CampaignStepDto of(final CampaignStepType type, final int stepOrder) {
    return new CampaignStepDto(type.name(), type.getLabel(), stepOrder);
  }

  public static List<CampaignStepDto> toDtoList(final List<CampaignStepType> types) {
    final List<CampaignStepDto> dtos = new ArrayList<>(types.size());
    for (int i = 0; i < types.size(); i++) {
      dtos.add(of(types.get(i), i + 1));
    }
    return dtos;
  }
}
