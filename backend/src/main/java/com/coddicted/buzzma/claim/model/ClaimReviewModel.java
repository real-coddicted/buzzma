package com.coddicted.buzzma.claim.model;

import com.coddicted.buzzma.campaign.entity.Campaign;
import com.coddicted.buzzma.claim.entity.Claim;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class ClaimReviewModel {
  private Claim claim;
  private Campaign campaign;
  private UUID dealOwnerId;
  private String dealOwnerName;
  private String buyerName;
}
