package com.coddicted.buzzma.campaign.service.impl;

import com.coddicted.buzzma.campaign.entity.CampaignType;
import com.coddicted.buzzma.campaign.entity.CampaignTypeStep;
import com.coddicted.buzzma.campaign.persistence.CampaignTypeStepRepository;
import com.coddicted.buzzma.campaign.service.CampaignTypeStepService;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class CampaignTypeStepServiceImpl implements CampaignTypeStepService {

  private final CampaignTypeStepRepository campaignTypeStepRepository;

  public CampaignTypeStepServiceImpl(final CampaignTypeStepRepository campaignTypeStepRepository) {
    this.campaignTypeStepRepository = campaignTypeStepRepository;
  }

  @Override
  public Map<CampaignType, List<CampaignTypeStep>> getStepConfig() {
    return campaignTypeStepRepository.findAll().stream()
        .sorted(Comparator.comparingInt(CampaignTypeStep::getStepOrder))
        .collect(
            Collectors.groupingBy(
                s -> s.getId().getCampaignType(), LinkedHashMap::new, Collectors.toList()));
  }
}
