package com.coddicted.buzzma.campaign.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.coddicted.buzzma.campaign.entity.Campaign;
import com.coddicted.buzzma.campaign.entity.CampaignStepType;
import java.util.List;
import org.junit.jupiter.api.Test;

class CampaignStepResolverImplTest {

  private final CampaignStepResolverImpl resolver = new CampaignStepResolverImpl();

  @Test
  void testResolveOrdersStepsByEnumOrdinalAndAppendsCashbackLast() {
    final Campaign campaign =
        Campaign.builder()
            .requiredSteps(
                List.of(
                    CampaignStepType.RETURN_WINDOW,
                    CampaignStepType.ORDER,
                    CampaignStepType.RATING))
            .build();

    final List<CampaignStepType> result = this.resolver.resolve(campaign);

    assertEquals(
        List.of(
            CampaignStepType.ORDER,
            CampaignStepType.RATING,
            CampaignStepType.RETURN_WINDOW,
            CampaignStepType.CASHBACK),
        result);
  }

  @Test
  void testResolveDeduplicatesRequiredSteps() {
    final Campaign campaign =
        Campaign.builder()
            .requiredSteps(List.of(CampaignStepType.ORDER, CampaignStepType.ORDER))
            .build();

    final List<CampaignStepType> result = this.resolver.resolve(campaign);

    assertEquals(List.of(CampaignStepType.ORDER, CampaignStepType.CASHBACK), result);
  }

  @Test
  void testResolveWithNullRequiredStepsOnlyReturnsCashback() {
    final Campaign campaign = Campaign.builder().requiredSteps(null).build();

    final List<CampaignStepType> result = this.resolver.resolve(campaign);

    assertEquals(List.of(CampaignStepType.CASHBACK), result);
  }
}
