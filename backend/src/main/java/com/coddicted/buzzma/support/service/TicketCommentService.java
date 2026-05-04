package com.coddicted.buzzma.support.service;

import com.coddicted.buzzma.support.entity.TicketComment;
import java.util.List;
import java.util.UUID;

public interface TicketCommentService {

  TicketComment addComment(TicketComment comment, UUID requesterId);

  List<TicketComment> listByTicketId(UUID ticketId);
}
