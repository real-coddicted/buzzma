package com.coddicted.buzzma.payment.service;

import com.coddicted.buzzma.payment.entity.Payment;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;

public interface PaymentService {

  Payment save(Payment payment);

  Payment getById(UUID paymentId);

  Map<UUID, Payment> findAllByIdAsMap(Collection<UUID> paymentIds);
}
