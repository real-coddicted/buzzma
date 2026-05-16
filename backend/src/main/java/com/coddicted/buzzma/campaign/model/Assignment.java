package com.coddicted.buzzma.campaign.model;

import com.coddicted.buzzma.campaign.entity.Campaign;
import com.coddicted.buzzma.campaign.entity.CampaignAssignment;
import com.coddicted.buzzma.campaign.entity.CampaignSlot;
import lombok.*;

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
