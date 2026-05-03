package com.coddicted.buzzma.support.service.impl;

import com.coddicted.buzzma.shared.common.BaseCrudService;
import com.coddicted.buzzma.shared.common.OffsetBasedPageRequest;
import com.coddicted.buzzma.support.api.TicketRequestDto;
import com.coddicted.buzzma.support.api.TicketResponseDto;
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
  public List<TicketResponseDto> list(int limit, int offset) {
    var pageable =
        new OffsetBasedPageRequest(limit, offset, Sort.by(Sort.Direction.DESC, "createdAt"));
    return repository.findAllByIsDeletedFalse(pageable).stream()
        .map(mapper::toResponse)
        .collect(Collectors.toList());
  }

  @Override
  @Transactional(readOnly = true)
  public TicketResponseDto getById(UUID id) {
    TicketsEntity entity = mustFind(repository, id, "Tickets");
    return mapper.toResponse(entity);
  }

  @Override
  @Transactional
  public TicketResponseDto create(TicketRequestDto request) {
    TicketsEntity entity = mapper.toEntity(request);
    return mapper.toResponse(repository.save(entity));
  }

  @Override
  @Transactional
  public TicketResponseDto update(UUID id, TicketRequestDto request) {
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
