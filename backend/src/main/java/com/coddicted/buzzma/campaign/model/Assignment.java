package com.coddicted.buzzma.campaign.model;

import com.coddicted.buzzma.campaign.entity.Campaign;
import com.coddicted.buzzma.campaign.entity.CampaignAssignment;
import com.coddicted.buzzma.campaign.entity.CampaignSlot;
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
public class Assignment {
  private Campaign campaign;
  private CampaignAssignment campaignAssignment;
  private CampaignSlot campaignSlot;
}
