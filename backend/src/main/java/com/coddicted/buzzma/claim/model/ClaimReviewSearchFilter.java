package com.coddicted.buzzma.claim.model;

import com.coddicted.buzzma.claim.dto.ClaimReviewFilterRequestDto;
import com.coddicted.buzzma.claim.entity.ClaimStatus;
import com.coddicted.buzzma.shared.enums.Platform;
import java.util.Set;
import java.util.UUID;

public record ClaimReviewSearchFilter(
    Set<UUID> campaignIds,
    Set<UUID> mediatorIds,
    Set<ClaimStatus> claimStatuses,
    Set<String> brands,
    Set<Platform> platforms,
    String claimCode) {

  public static ClaimReviewSearchFilter from(final ClaimReviewFilterRequestDto dto) {
    if (dto == null) {
      return new ClaimReviewSearchFilter(null, null, null, null, null, null);
    }
    return new ClaimReviewSearchFilter(
        dto.getCampaignIds(),
        dto.getMediatorIds(),
        dto.getClaimStatuses(),
        dto.getBrands(),
        dto.getPlatforms(),
        dto.getClaimCode());
  }
}
