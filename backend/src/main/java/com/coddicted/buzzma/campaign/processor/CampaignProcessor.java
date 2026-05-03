package com.coddicted.buzzma.campaign.processor;

import com.coddicted.buzzma.campaign.dto.CampaignRequestDto;
import com.coddicted.buzzma.campaign.dto.CampaignResponseDto;
import com.coddicted.buzzma.campaign.entity.Campaign;
import com.coddicted.buzzma.campaign.entity.Product;
import com.coddicted.buzzma.campaign.mapper.CampaignMapper;
import com.coddicted.buzzma.campaign.service.CampaignService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Component;

@Component
public class CampaignProcessor {

  private final CampaignService service;
  private final CampaignMapper campaignMapper;

  private final ProductProcessor productProcessor;

  public CampaignProcessor(
      CampaignService service, CampaignMapper campaignMapper, ProductProcessor productProcessor) {
    this.service = service;
    this.campaignMapper = campaignMapper;
    this.productProcessor = productProcessor;
  }

  @Transactional
  public CampaignResponseDto create(final CampaignRequestDto request) {

    // save product for this campaign first
    Product newProduct = productProcessor.saveProduct(request);

    Campaign newCampaign = service.create(campaignMapper.toCampaignEntity(request));

    return null;
  }
}
