package com.coddicted.buzzma.campaign.service;

import com.coddicted.buzzma.campaign.api.ProductResponseDto;

import java.util.UUID;

/**
 * products are created and updated as the part of the campaign create and update request, so we only need to retrieve the product details by id.
 */
public interface ProductService {
    ProductResponseDto getById(UUID productId);
}
