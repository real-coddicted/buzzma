package com.coddicted.buzzma.order.service;

import com.coddicted.buzzma.order.entity.Order;
import java.util.List;
import java.util.UUID;

public interface OrderService {

  Order createOrder(Order order, byte[] screenshot, String screenshotFilename, String contentType);

  Order submitReview(
      UUID orderId,
      UUID buyerId,
      String reviewUrl,
      byte[] screenshot,
      String filename,
      String contentType);

  Order submitReturn(
      UUID orderId, UUID buyerId, byte[] screenshot, String filename, String contentType);

  Order getById(UUID orderId, UUID buyerId);

  List<Order> listByBuyer(UUID buyerId);
}
