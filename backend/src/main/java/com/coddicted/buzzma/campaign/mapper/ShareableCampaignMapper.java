package com.coddicted.buzzma.campaign.mapper;

import com.coddicted.buzzma.campaign.dto.ShareableCampaignResponseDto;
import com.coddicted.buzzma.campaign.persistence.ShareableCampaignView;
import java.util.List;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ShareableCampaignMapper {

  ShareableCampaignResponseDto toResponse(ShareableCampaignView view);

  List<ShareableCampaignResponseDto> toResponse(List<ShareableCampaignView> views);
}
