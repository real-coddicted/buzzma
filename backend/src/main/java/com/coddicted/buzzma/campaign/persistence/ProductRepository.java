package com.coddicted.buzzma.campaign.persistence;

import com.coddicted.buzzma.campaign.entity.Product;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {}
