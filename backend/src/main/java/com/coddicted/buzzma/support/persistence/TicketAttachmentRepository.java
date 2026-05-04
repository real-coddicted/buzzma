package com.coddicted.buzzma.support.persistence;

import com.coddicted.buzzma.support.entity.TicketAttachment;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketAttachmentRepository extends JpaRepository<TicketAttachment, UUID> {

  List<TicketAttachment> findAllByTicketIdAndIsDeletedFalse(UUID ticketId);
}
