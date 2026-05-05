package com.coddicted.buzzma.campaign.processor;

import com.coddicted.buzzma.campaign.dto.CampaignRequestDto;
import com.coddicted.buzzma.campaign.entity.Product;
import com.coddicted.buzzma.campaign.service.ProductService;
import org.springframework.stereotype.Component;

@Component
public class ProductProcessor {

  private final ProductService productService;

  public ProductProcessor(ProductService productService) {
    this.productService = productService;
  }

  public Product saveProduct(CampaignRequestDto request) {
    Product newProduct = Product.builder().name(request.getProductName()).build();

    return productService.create(newProduct);
  }
}
