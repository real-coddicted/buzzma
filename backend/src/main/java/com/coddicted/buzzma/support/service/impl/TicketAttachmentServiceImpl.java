package com.coddicted.buzzma.support.service.impl;

import com.coddicted.buzzma.shared.common.BaseCrudService;
import com.coddicted.buzzma.shared.storage.StorageService;
import com.coddicted.buzzma.support.entity.TicketAttachment;
import com.coddicted.buzzma.support.persistence.TicketAttachmentRepository;
import com.coddicted.buzzma.support.persistence.TicketRepository;
import com.coddicted.buzzma.support.service.TicketAttachmentService;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TicketAttachmentServiceImpl extends BaseCrudService
    implements TicketAttachmentService {

  private static final String FOLDER = "tickets";

  private final TicketAttachmentRepository attachmentRepository;
  private final TicketRepository ticketRepository;
  private final StorageService storageService;

  public TicketAttachmentServiceImpl(
      final TicketAttachmentRepository attachmentRepository,
      final TicketRepository ticketRepository,
      final StorageService storageService) {
    this.attachmentRepository = attachmentRepository;
    this.ticketRepository = ticketRepository;
    this.storageService = storageService;
  }

  @Override
  @Transactional
  public TicketAttachment upload(
      final UUID ticketId,
      final String fileName,
      final String contentType,
      final byte[] data,
      final UUID requesterId) {
    mustFind(ticketRepository, ticketId, "Ticket");
    final String storageKey = storageService.store(FOLDER, fileName, contentType, data);
    final TicketAttachment attachment =
        TicketAttachment.builder()
            .ticketId(ticketId)
            .uploadedBy(requesterId)
            .fileName(fileName)
            .contentType(contentType)
            .sizeBytes((long) data.length)
            .storageKey(storageKey)
            .isDeleted(false)
            .createdBy(requesterId)
            .updatedBy(requesterId)
            .build();
    return attachmentRepository.save(attachment);
  }

  @Override
  @Transactional(readOnly = true)
  public List<TicketAttachment> listByTicketId(final UUID ticketId) {
    return attachmentRepository.findAllByTicketIdAndIsDeletedFalse(ticketId);
  }
}
