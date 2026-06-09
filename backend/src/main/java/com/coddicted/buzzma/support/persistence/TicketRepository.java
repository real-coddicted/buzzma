package com.coddicted.buzzma.support.persistence;

import com.coddicted.buzzma.support.entity.Ticket;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketRepository extends JpaRepository<Ticket, UUID> {

  Page<Ticket> findAllByRaisedByAndIsDeletedFalse(UUID raisedBy, Pageable pageable);

  Page<Ticket> findAllByAssigneeIdAndIsDeletedFalse(UUID assigneeId, Pageable pageable);

  Page<Ticket> findAllByIsDeletedFalse(Pageable pageable);
}
