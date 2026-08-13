package com.coddicted.buzzma.campaign.mapper;

import com.coddicted.buzzma.campaign.dto.SharedCampaignViewResponseDto;
import com.coddicted.buzzma.campaign.persistence.SharedCampaignSummaryView;
import java.util.List;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SharedCampaignSummaryMapper {

  SharedCampaignViewResponseDto toResponse(SharedCampaignSummaryView view);

  List<SharedCampaignViewResponseDto> toResponse(List<SharedCampaignSummaryView> views);
}
