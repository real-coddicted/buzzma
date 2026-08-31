package com.coddicted.buzzma.campaign.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.coddicted.buzzma.campaign.dto.CampaignRequestDto;
import com.coddicted.buzzma.campaign.entity.Product;
import java.net.URI;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class ProductMapperTest {

  private final ProductMapper productMapper = Mappers.getMapper(ProductMapper.class);

  @Test
  void wrapsSingleImageUrlIntoOneElementList() throws Exception {
    final CampaignRequestDto request =
        CampaignRequestDto.builder().productImageUrl("https://example.com/image.jpg").build();

    final Product product = this.productMapper.toProductEntity(request);

    assertEquals(List.of(new URI("https://example.com/image.jpg").toURL()), product.getImageUrls());
  }

  @Test
  void mapsNullImageUrlToEmptyList() {
    final CampaignRequestDto request = CampaignRequestDto.builder().productImageUrl(null).build();

    final Product product = this.productMapper.toProductEntity(request);

    assertTrue(product.getImageUrls().isEmpty());
  }

  @Test
  void mapsBlankImageUrlToEmptyList() {
    final CampaignRequestDto request = CampaignRequestDto.builder().productImageUrl("  ").build();

    final Product product = this.productMapper.toProductEntity(request);

    assertTrue(product.getImageUrls().isEmpty());
  }
}
