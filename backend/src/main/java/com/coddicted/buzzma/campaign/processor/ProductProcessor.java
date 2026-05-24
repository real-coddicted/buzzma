package com.coddicted.buzzma.campaign.processor;

import com.coddicted.buzzma.campaign.dto.CampaignRequestDto;
import com.coddicted.buzzma.campaign.entity.Product;
import com.coddicted.buzzma.campaign.mapper.ProductMapper;
import com.coddicted.buzzma.campaign.service.ProductService;
import org.springframework.stereotype.Component;

@Component
public class ProductProcessor {

  private final ProductService productService;
  private final ProductMapper productMapper;

  public ProductProcessor(ProductService productService, ProductMapper productMapper) {
    this.productService = productService;
    this.productMapper = productMapper;
  }

  public Product saveProduct(CampaignRequestDto request) {
    return productService.create(productMapper.toProductEntity(request));
  }
}
