package com.coddicted.buzzma.support.persistence;

import com.coddicted.buzzma.support.entity.Ticket;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketRepository extends JpaRepository<Ticket, UUID> {

  List<Ticket> findAllByRaisedByAndIsDeletedFalse(UUID raisedBy);
}
