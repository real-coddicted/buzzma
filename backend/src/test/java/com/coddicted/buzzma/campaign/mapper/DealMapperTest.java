package com.coddicted.buzzma.campaign.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.coddicted.buzzma.campaign.dto.DealResponseDto;
import com.coddicted.buzzma.campaign.entity.Campaign;
import com.coddicted.buzzma.campaign.entity.Deal;
import com.coddicted.buzzma.campaign.entity.ExchangeProduct;
import com.coddicted.buzzma.campaign.entity.Product;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class DealMapperTest {

  private final DealMapper dealMapper = Mappers.getMapper(DealMapper.class);

  @Test
  void toDealResponseUsesAffiliateUrlWhenPresent()
      throws MalformedURLException, URISyntaxException {
    final Deal deal = dealWithAffiliateUrl("https://affiliate.example.com/track");

    final DealResponseDto response = this.dealMapper.toDealResponse(deal);

    assertEquals("https://affiliate.example.com/track", response.getProductUrl());
  }

  @Test
  void toDealResponseFallsBackToProductLinkWhenAffiliateUrlBlank()
      throws MalformedURLException, URISyntaxException {
    final Deal deal = dealWithAffiliateUrl("   ");

    final DealResponseDto response = this.dealMapper.toDealResponse(deal);

    assertEquals("https://example.com/product", response.getProductUrl());
  }

  @Test
  void toDealResponseFallsBackToProductLinkWhenAffiliateUrlNull()
      throws MalformedURLException, URISyntaxException {
    final Deal deal = dealWithAffiliateUrl(null);

    final DealResponseDto response = this.dealMapper.toDealResponse(deal);

    assertEquals("https://example.com/product", response.getProductUrl());
  }

  @Test
  void toDealResponseMapsCampaignStartAndEndDate()
      throws MalformedURLException, URISyntaxException {
    final Product product =
        Product.builder()
            .name("Test Product")
            .productLink(new URI("https://example.com/product").toURL())
            .pricePaise(BigInteger.valueOf(99900))
            .build();
    final Campaign campaign =
        Campaign.builder().product(product).startDate(20260901).endDate(20260930).build();
    final Deal deal =
        Deal.builder().campaign(campaign).dealPricePaise(BigInteger.valueOf(49900)).build();

    final DealResponseDto response = this.dealMapper.toDealResponse(deal);

    assertEquals(20260901, response.getStartDate());
    assertEquals(20260930, response.getEndDate());
  }

  @Test
  void toDealResponseCarriesCampaignExchangeProducts()
      throws MalformedURLException, URISyntaxException {
    final Product product =
        Product.builder()
            .name("Test Product")
            .productLink(new URI("https://example.com/product").toURL())
            .pricePaise(BigInteger.valueOf(99900))
            .build();
    final ExchangeProduct exchangeProduct =
        ExchangeProduct.builder()
            .productName("Old Blender")
            .productImageUrl(new URI("https://example.com/old-blender.png").toURL())
            .build();
    final Campaign campaign =
        Campaign.builder().product(product).exchangeProducts(List.of(exchangeProduct)).build();
    final Deal deal =
        Deal.builder().campaign(campaign).dealPricePaise(BigInteger.valueOf(49900)).build();

    final DealResponseDto response = this.dealMapper.toDealResponse(deal);

    assertEquals(List.of(exchangeProduct), response.getExchangeProducts());
  }

  private Deal dealWithAffiliateUrl(final String affiliateUrl)
      throws MalformedURLException, URISyntaxException {
    final Product product =
        Product.builder()
            .name("Test Product")
            .productLink(new URI("https://example.com/product").toURL())
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
