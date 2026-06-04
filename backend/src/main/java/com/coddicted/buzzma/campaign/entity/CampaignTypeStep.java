package com.coddicted.buzzma.campaign.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;

@Getter
@Entity
@Table(name = "campaign_type_step")
public class CampaignTypeStep {

  @EmbeddedId private CampaignTypeStepId id;

  @Column(name = "step_order", nullable = false)
  private int stepOrder;
}
