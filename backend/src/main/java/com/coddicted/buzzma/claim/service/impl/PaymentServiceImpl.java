package com.coddicted.buzzma.claim.service.impl;

import com.coddicted.buzzma.claim.entity.Payment;
import com.coddicted.buzzma.claim.persistence.PaymentRepository;
import com.coddicted.buzzma.claim.service.PaymentService;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PaymentServiceImpl implements PaymentService {

  private final PaymentRepository paymentRepository;

  public PaymentServiceImpl(final PaymentRepository paymentRepository) {
    this.paymentRepository = paymentRepository;
  }

  @Override
  @Transactional
  public Payment save(final Payment payment) {
    // Flushed immediately so the row is visible to the bulk ClaimAccounting update that follows
    // within the same transaction (its FK reference to this payment must already exist).
    return paymentRepository.saveAndFlush(payment);
  }

  @Override
  @Transactional(readOnly = true)
  public Payment getById(final UUID paymentId) {
    return paymentRepository
        .findById(paymentId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment not found"));
  }

  @Override
  @Transactional(readOnly = true)
  public Map<UUID, Payment> findAllByIdAsMap(final Collection<UUID> paymentIds) {
    return paymentRepository.findAllById(paymentIds).stream()
        .collect(Collectors.toMap(Payment::getId, Function.identity()));
  }
}
