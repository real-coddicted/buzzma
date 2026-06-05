package com.coddicted.buzzma.campaign.mapper;

import com.coddicted.buzzma.campaign.dto.CampaignStepDto;
import com.coddicted.buzzma.campaign.entity.CampaignType;
import com.coddicted.buzzma.campaign.entity.CampaignTypeStep;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CampaignTypeStepMapper {

  @Mapping(source = "id.stepType", target = "type")
  @Mapping(source = "id.stepType.label", target = "label")
  @Mapping(source = "stepOrder", target = "stepOrder")
  CampaignStepDto toDto(CampaignTypeStep step);

  List<CampaignStepDto> toDtoList(List<CampaignTypeStep> steps);

  default Map<String, List<CampaignStepDto>> toCampaignStepDtoMap(
      Map<CampaignType, List<CampaignTypeStep>> stepConfig) {
    return stepConfig.entrySet().stream()
        .collect(
            Collectors.toMap(
                e -> e.getKey().name(),
                e -> toDtoList(e.getValue()),
                (a, b) -> a,
                LinkedHashMap::new));
  }
}
