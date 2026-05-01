package com.coddicted.buzzma.campaign.persistence;

import com.coddicted.buzzma.campaign.entity.Product;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ProductRepository {
    Product findById(UUID productId);
}
