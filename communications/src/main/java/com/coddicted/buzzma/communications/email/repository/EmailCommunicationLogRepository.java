package com.coddicted.buzzma.communications.email.repository;

import com.coddicted.buzzma.communications.email.model.EmailCommunicationLog;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmailCommunicationLogRepository
    extends JpaRepository<EmailCommunicationLog, UUID> {}
