package com.coddicted.buzzma.payment.persistence;

import com.coddicted.buzzma.payment.entity.Payment;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {}
