package com.coddicted.buzzma.campaign.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.coddicted.buzzma.campaign.dto.DealResponseDto;
import com.coddicted.buzzma.campaign.entity.Campaign;
import com.coddicted.buzzma.campaign.entity.Deal;
import com.coddicted.buzzma.campaign.entity.Product;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class DealMapperTest {

  private final DealMapper dealMapper = Mappers.getMapper(DealMapper.class);

  @Test
  void toDealResponseUsesAffiliateUrlWhenPresent() throws MalformedURLException {
    final Deal deal = dealWithAffiliateUrl("https://affiliate.example.com/track");

    final DealResponseDto response = this.dealMapper.toDealResponse(deal);

    assertEquals("https://affiliate.example.com/track", response.getProductUrl());
  }

  @Test
  void toDealResponseFallsBackToProductLinkWhenAffiliateUrlBlank() throws MalformedURLException {
    final Deal deal = dealWithAffiliateUrl("   ");

    final DealResponseDto response = this.dealMapper.toDealResponse(deal);

    assertEquals("https://example.com/product", response.getProductUrl());
  }

  @Test
  void toDealResponseFallsBackToProductLinkWhenAffiliateUrlNull() throws MalformedURLException {
    final Deal deal = dealWithAffiliateUrl(null);

    final DealResponseDto response = this.dealMapper.toDealResponse(deal);

    assertEquals("https://example.com/product", response.getProductUrl());
  }

  private Deal dealWithAffiliateUrl(final String affiliateUrl) throws MalformedURLException {
    final Product product =
        Product.builder()
            .name("Test Product")
            .productLink(new URL("https://example.com/product"))
            .pricePaise(BigInteger.valueOf(99900))
            .build();
    final Campaign campaign = Campaign.builder().product(product).build();
    return Deal.builder()
        .campaign(campaign)
        .dealPricePaise(BigInteger.valueOf(49900))
        .affiliateUrl(affiliateUrl)
        .build();
  }
}
