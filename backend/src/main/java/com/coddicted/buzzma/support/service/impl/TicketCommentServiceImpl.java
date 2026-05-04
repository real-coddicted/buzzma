package com.coddicted.buzzma.support.service.impl;

import com.coddicted.buzzma.shared.common.BaseCrudService;
import com.coddicted.buzzma.support.entity.TicketComment;
import com.coddicted.buzzma.support.persistence.TicketCommentRepository;
import com.coddicted.buzzma.support.persistence.TicketRepository;
import com.coddicted.buzzma.support.service.TicketCommentService;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TicketCommentServiceImpl extends BaseCrudService implements TicketCommentService {

  private final TicketCommentRepository commentRepository;
  private final TicketRepository ticketRepository;

  public TicketCommentServiceImpl(
      final TicketCommentRepository commentRepository, final TicketRepository ticketRepository) {
    this.commentRepository = commentRepository;
    this.ticketRepository = ticketRepository;
  }

  @Override
  @Transactional
  public TicketComment addComment(final TicketComment comment, final UUID requesterId) {
    mustFind(ticketRepository, comment.getTicketId(), "Ticket");
    final TicketComment toSave =
        comment.toBuilder()
            .authorId(requesterId)
            .isDeleted(false)
            .createdBy(requesterId)
            .updatedBy(requesterId)
            .build();
    return commentRepository.save(toSave);
  }

  @Override
  @Transactional(readOnly = true)
  public List<TicketComment> listByTicketId(final UUID ticketId) {
    mustFind(ticketRepository, ticketId, "Ticket");
    return commentRepository.findAllByTicketIdAndIsDeletedFalseOrderByCreatedAtAsc(ticketId);
  }
}
