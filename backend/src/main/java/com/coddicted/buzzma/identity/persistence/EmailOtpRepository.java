package com.coddicted.buzzma.identity.persistence;

import com.coddicted.buzzma.identity.entity.EmailOtp;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmailOtpRepository extends JpaRepository<EmailOtp, UUID> {

  Optional<EmailOtp> findTopByUserIdOrderByCreatedAtDesc(UUID userId);
}
