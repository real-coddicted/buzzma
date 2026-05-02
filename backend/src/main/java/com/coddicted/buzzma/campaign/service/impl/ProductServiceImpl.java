package com.coddicted.buzzma.campaign.service.impl;

import com.coddicted.buzzma.campaign.entity.Product;
import com.coddicted.buzzma.campaign.persistence.ProductRepository;
import com.coddicted.buzzma.campaign.service.ProductService;
import com.coddicted.buzzma.shared.common.BaseCrudService;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class ProductServiceImpl extends BaseCrudService implements ProductService {

  private final ProductRepository productRepository;

  public ProductServiceImpl(final ProductRepository productRepository) {
    this.productRepository = productRepository;
  }

  @Override
  public Product getById(UUID productId) {
    return this.productRepository.findById(productId);
  }
}
