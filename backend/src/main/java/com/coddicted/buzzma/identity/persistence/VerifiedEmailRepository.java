package com.coddicted.buzzma.identity.persistence;

import com.coddicted.buzzma.identity.entity.VerifiedEmail;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VerifiedEmailRepository extends JpaRepository<VerifiedEmail, UUID> {}
