package com.coddicted.buzzma.campaign.model;

import com.coddicted.buzzma.campaign.entity.Campaign;
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
public class CampaignSummary {
  private Campaign campaign;
  private Integer slotsClaimed;

  /** Populated only for campaigns shared with the requester; null otherwise. */
  private String campaignOwnerName;
}
