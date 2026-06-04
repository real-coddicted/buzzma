package com.coddicted.buzzma.campaign.persistence;

import com.coddicted.buzzma.campaign.entity.CampaignTypeStep;
import com.coddicted.buzzma.campaign.entity.CampaignTypeStepId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CampaignTypeStepRepository
    extends JpaRepository<CampaignTypeStep, CampaignTypeStepId> {}
