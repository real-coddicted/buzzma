package com.coddicted.buzzma.campaign.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class AssignToMediatorRequestDto {
  @NotNull UUID mediatorId;
  @NotEmpty List<@Valid MediatorCampaignAssignmentItemDto> campaigns;
}
