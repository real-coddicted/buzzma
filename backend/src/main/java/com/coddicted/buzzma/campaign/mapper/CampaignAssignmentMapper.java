package com.coddicted.buzzma.campaign.mapper;

import com.coddicted.buzzma.campaign.dto.CampaignAssignmentRequestDto;
import com.coddicted.buzzma.campaign.dto.CampaignAssignmentResponseDto;
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
  @Mapping(target = "campaignSlot", ignore = true)
  @Mapping(target = "isDeleted", ignore = true)
  @Mapping(target = "createdBy", ignore = true)
  @Mapping(target = "updatedBy", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  @Mapping(target = "status", ignore = true)
  @Mapping(source = "slotOffered", target = "slotLimit")
  CampaignAssignment toCampaignAssignment(CampaignAssignmentRequestDto request);

  List<CampaignAssignment> toCampaignAssignments(List<CampaignAssignmentRequestDto> request);

  @Mapping(source = "slotLimit", target = "slotOffered")
  CampaignAssignmentResponseDto toResponse(CampaignAssignment entity);

  List<CampaignAssignmentResponseDto> toResponse(List<CampaignAssignment> entities);
}
