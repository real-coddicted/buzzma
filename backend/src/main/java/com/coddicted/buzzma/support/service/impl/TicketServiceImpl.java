package com.coddicted.buzzma.support.service.impl;

import com.coddicted.buzzma.shared.common.BaseCrudService;
import com.coddicted.buzzma.shared.common.OffsetBasedPageRequest;
import com.coddicted.buzzma.support.api.TicketsRequestDto;
import com.coddicted.buzzma.support.api.TicketsResponseDto;
import com.coddicted.buzzma.support.mapper.TicketsMapper;
import com.coddicted.buzzma.support.persistence.TicketsEntity;
import com.coddicted.buzzma.support.persistence.TicketsRepository;
import com.coddicted.buzzma.support.service.TicketService;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TicketServiceImpl extends BaseCrudService implements TicketService {

  private final TicketsRepository repository;
  private final TicketsMapper mapper;

  public TicketServiceImpl(TicketsRepository repository, TicketsMapper mapper) {
    this.repository = repository;
    this.mapper = mapper;
  }

  @Override
  @Transactional(readOnly = true)
  public List<TicketsResponseDto> list(int limit, int offset) {
    var pageable =
        new OffsetBasedPageRequest(limit, offset, Sort.by(Sort.Direction.DESC, "createdAt"));
    return repository.findAllByIsDeletedFalse(pageable).stream()
        .map(mapper::toResponse)
        .collect(Collectors.toList());
  }

  @Override
  @Transactional(readOnly = true)
  public TicketsResponseDto getById(UUID id) {
    TicketsEntity entity = mustFind(repository, id, "Tickets");
    return mapper.toResponse(entity);
  }

  @Override
  @Transactional
  public TicketsResponseDto create(TicketsRequestDto request) {
    TicketsEntity entity = mapper.toEntity(request);
    return mapper.toResponse(repository.save(entity));
  }

  @Override
  @Transactional
  public TicketsResponseDto update(UUID id, TicketsRequestDto request) {
    TicketsEntity entity = mustFind(repository, id, "Tickets");
    mapper.update(request, entity);
    return mapper.toResponse(repository.save(entity));
  }

  @Override
  @Transactional
  public void delete(UUID id) {
    TicketsEntity entity = mustFind(repository, id, "Tickets");
    entity.setIsDeleted(true);
    repository.save(entity);
  }
}
