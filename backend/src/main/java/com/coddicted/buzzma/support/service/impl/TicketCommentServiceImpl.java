package com.coddicted.buzzma.support.service.impl;

import com.coddicted.buzzma.shared.common.BaseCrudService;
import com.coddicted.buzzma.shared.common.OffsetBasedPageRequest;
import com.coddicted.buzzma.support.api.TicketCommentRequestDto;
import com.coddicted.buzzma.support.api.TicketCommentResponseDto;
import com.coddicted.buzzma.support.mapper.TicketCommentsMapper;
import com.coddicted.buzzma.support.persistence.TicketCommentsEntity;
import com.coddicted.buzzma.support.persistence.TicketCommentsRepository;
import com.coddicted.buzzma.support.service.TicketCommentService;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TicketCommentServiceImpl extends BaseCrudService implements TicketCommentService {

  private final TicketCommentsRepository repository;
  private final TicketCommentsMapper mapper;

  public TicketCommentServiceImpl(
      TicketCommentsRepository repository, TicketCommentsMapper mapper) {
    this.repository = repository;
    this.mapper = mapper;
  }

  @Override
  @Transactional(readOnly = true)
  public List<TicketCommentResponseDto> list(int limit, int offset) {
    var pageable =
        new OffsetBasedPageRequest(limit, offset, Sort.by(Sort.Direction.DESC, "createdAt"));
    return repository.findAll(pageable).stream()
        .map(mapper::toResponse)
        .collect(Collectors.toList());
  }

  @Override
  @Transactional(readOnly = true)
  public TicketCommentResponseDto getById(UUID id) {
    TicketCommentsEntity entity = mustFind(repository, id, "TicketComments");
    return mapper.toResponse(entity);
  }

  @Override
  @Transactional
  public TicketCommentResponseDto create(TicketCommentRequestDto request) {
    TicketCommentsEntity entity = mapper.toEntity(request);
    return mapper.toResponse(repository.save(entity));
  }

  @Override
  @Transactional
  public TicketCommentResponseDto update(UUID id, TicketCommentRequestDto request) {
    TicketCommentsEntity entity = mustFind(repository, id, "TicketComments");
    mapper.update(request, entity);
    return mapper.toResponse(repository.save(entity));
  }

  @Override
  @Transactional
  public void delete(UUID id) {
    TicketCommentsEntity entity = mustFind(repository, id, "TicketComments");
    entity.setIsDeleted(true);
    repository.save(entity);
  }
}
