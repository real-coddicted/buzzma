package com.coddicted.buzzma.campaign.mapper;

import com.coddicted.buzzma.campaign.dto.SharedCampaignResponseDto;
import com.coddicted.buzzma.campaign.persistence.SharedCampaignView;
import java.util.List;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SharedCampaignMapper {

  SharedCampaignResponseDto toResponse(SharedCampaignView view);

  List<SharedCampaignResponseDto> toResponse(List<SharedCampaignView> views);
}
