package com.coddicted.buzzma.claim.dto;

import com.coddicted.buzzma.claim.entity.ClaimStatus;
import jakarta.annotation.Nullable;
import java.util.Set;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class ClaimReviewFilterRequestDto {

  @Nullable Set<UUID> campaignIds;

  // Only applicable when filtering as an agency. Ignored entirely on the mediator endpoint,
  // since a mediator can only ever see their own claims.
  @Nullable Set<UUID> mediatorIds;

  @Nullable Set<ClaimStatus> claimStatuses;
}
