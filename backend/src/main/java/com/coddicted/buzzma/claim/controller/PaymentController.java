package com.coddicted.buzzma.claim.controller;

import com.coddicted.buzzma.claim.dto.PaymentReceiptDto;
import com.coddicted.buzzma.claim.mapper.PaymentMapper;
import com.coddicted.buzzma.claim.service.MyPaymentsService;
import com.coddicted.buzzma.shared.security.CurrentUserId;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {

  private final MyPaymentsService myPaymentsService;
  private final PaymentMapper paymentMapper;

  public PaymentController(
      final MyPaymentsService myPaymentsService, final PaymentMapper paymentMapper) {
    this.myPaymentsService = myPaymentsService;
    this.paymentMapper = paymentMapper;
  }

  @GetMapping("/{id}")
  public PaymentReceiptDto getReceipt(
      @CurrentUserId final UUID callerId, @PathVariable final UUID id) {
    return paymentMapper.toDto(myPaymentsService.getReceipt(id, callerId));
  }
}
