package com.coddicted.buzzma.order.persistence;

import com.coddicted.buzzma.order.entity.Order;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {

  List<Order> findByBuyerIdAndIsDeletedFalse(UUID buyerId);

  Optional<Order> findByIdAndIsDeletedFalse(UUID id);

  boolean existsByBuyerIdAndCampaignIdAndIsDeletedFalse(UUID buyerId, UUID campaignId);
}
