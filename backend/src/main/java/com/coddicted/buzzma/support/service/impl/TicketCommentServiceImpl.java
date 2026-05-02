package com.coddicted.buzzma.support.service.impl;

import com.coddicted.buzzma.shared.common.BaseCrudService;
import com.coddicted.buzzma.shared.common.OffsetBasedPageRequest;
import com.coddicted.buzzma.support.api.TicketCommentsRequestDto;
import com.coddicted.buzzma.support.api.TicketCommentsResponseDto;
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
  public List<TicketCommentsResponseDto> list(int limit, int offset) {
    var pageable =
        new OffsetBasedPageRequest(limit, offset, Sort.by(Sort.Direction.DESC, "createdAt"));
    return repository.findAll(pageable).stream()
        .map(mapper::toResponse)
        .collect(Collectors.toList());
  }

  @Override
  @Transactional(readOnly = true)
  public TicketCommentsResponseDto getById(UUID id) {
    TicketCommentsEntity entity = mustFind(repository, id, "TicketComments");
    return mapper.toResponse(entity);
  }

  @Override
  @Transactional
  public TicketCommentsResponseDto create(TicketCommentsRequestDto request) {
    TicketCommentsEntity entity = mapper.toEntity(request);
    return mapper.toResponse(repository.save(entity));
  }

  @Override
  @Transactional
  public TicketCommentsResponseDto update(UUID id, TicketCommentsRequestDto request) {
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
