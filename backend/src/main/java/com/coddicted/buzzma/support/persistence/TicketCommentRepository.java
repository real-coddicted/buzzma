package com.coddicted.buzzma.support.persistence;

import com.coddicted.buzzma.support.entity.TicketComment;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketCommentRepository extends JpaRepository<TicketComment, UUID> {

  List<TicketComment> findAllByTicketIdAndIsDeletedFalseOrderByCreatedAtAsc(UUID ticketId);
}
