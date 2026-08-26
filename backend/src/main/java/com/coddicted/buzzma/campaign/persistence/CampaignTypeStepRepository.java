package com.coddicted.buzzma.campaign.persistence;

import com.coddicted.buzzma.campaign.entity.CampaignTypeStep;
import com.coddicted.buzzma.campaign.entity.CampaignTypeStepId;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @see CampaignTypeStep
 */
@Deprecated
public interface CampaignTypeStepRepository
    extends JpaRepository<CampaignTypeStep, CampaignTypeStepId> {}
