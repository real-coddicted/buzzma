package com.coddicted.buzzma.campaign.service.impl;

import com.coddicted.buzzma.campaign.entity.Campaign;
import com.coddicted.buzzma.campaign.entity.CampaignStepType;
import com.coddicted.buzzma.campaign.service.CampaignStepResolver;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class CampaignStepResolverImpl implements CampaignStepResolver {

  @Override
  public List<CampaignStepType> resolve(final Campaign campaign) {
    final List<CampaignStepType> required = campaign.getRequiredSteps();
    final List<CampaignStepType> ordered =
        (required == null ? List.<CampaignStepType>of() : required)
            .stream().distinct().sorted(Comparator.comparingInt(Enum::ordinal)).toList();
    final List<CampaignStepType> steps = new ArrayList<>(ordered);
    steps.add(CampaignStepType.CASHBACK);
    return steps;
  }
}
