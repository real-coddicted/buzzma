package com.coddicted.buzzma.campaign.mapper;

import com.coddicted.buzzma.campaign.api.CampaignAssignmentRequestDto;
import com.coddicted.buzzma.campaign.api.CampaignAssignmentResponseDto;
import com.coddicted.buzzma.campaign.entity.CampaignAssignment;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface CampaignAssignmentMapper {

  // ── CampaignAssignment (initial template — campaignId/assignee set by service) ──

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "campaignId", ignore = true)
  @Mapping(target = "isDeleted", ignore = true)
  @Mapping(source = "commissionOfferedPaise", target = "commissionOfferedPaise")
  CampaignAssignment toCampaignAssignment(CampaignAssignmentRequestDto request);

  List<CampaignAssignment> toCampaignAssignments(List<CampaignAssignmentRequestDto> request);

  CampaignAssignmentResponseDto toResponse(CampaignAssignment entity);

  List<CampaignAssignmentResponseDto> toResponse(List<CampaignAssignment> entities);
}
