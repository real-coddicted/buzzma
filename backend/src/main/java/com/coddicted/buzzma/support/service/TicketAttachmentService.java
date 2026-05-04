package com.coddicted.buzzma.support.service;

import com.coddicted.buzzma.support.entity.TicketAttachment;
import java.util.List;
import java.util.UUID;

public interface TicketAttachmentService {

  TicketAttachment upload(
      UUID ticketId, String fileName, String contentType, byte[] data, UUID requesterId);

  List<TicketAttachment> listByTicketId(UUID ticketId);
}
