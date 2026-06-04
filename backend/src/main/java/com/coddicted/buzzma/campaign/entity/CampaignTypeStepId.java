package com.coddicted.buzzma.campaign.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Embeddable
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class CampaignTypeStepId implements Serializable {

  @Enumerated(EnumType.STRING)
  @Column(name = "campaign_type", nullable = false)
  private CampaignType campaignType;

  @Enumerated(EnumType.STRING)
  @Column(name = "step_type", nullable = false)
  private CampaignStepType stepType;
}
