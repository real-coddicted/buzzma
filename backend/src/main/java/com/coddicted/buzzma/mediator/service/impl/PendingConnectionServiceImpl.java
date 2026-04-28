package com.coddicted.buzzma.mediator.service.impl;

import com.coddicted.buzzma.mediator.api.PendingConnectionsRequestDto;
import com.coddicted.buzzma.mediator.api.PendingConnectionsResponseDto;
import com.coddicted.buzzma.mediator.mapper.PendingConnectionsMapper;
import com.coddicted.buzzma.mediator.persistence.PendingConnectionsEntity;
import com.coddicted.buzzma.mediator.persistence.PendingConnectionsRepository;
import com.coddicted.buzzma.mediator.service.PendingConnectionService;
import com.coddicted.buzzma.shared.common.OffsetBasedPageRequest;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PendingConnectionServiceImpl extends BaseCrudService
    implements PendingConnectionService {

  private final PendingConnectionsRepository repository;
  private final PendingConnectionsMapper mapper;

  public PendingConnectionServiceImpl(
      PendingConnectionsRepository repository, PendingConnectionsMapper mapper) {
    this.repository = repository;
    this.mapper = mapper;
  }

  @Override
  @Transactional(readOnly = true)
  public List<PendingConnectionsResponseDto> list(int limit, int offset) {
    var pageable =
        new OffsetBasedPageRequest(limit, offset, Sort.by(Sort.Direction.DESC, "timestamp"));
    return repository.findAllByIsDeletedFalse(pageable).stream()
        .map(mapper::toResponse)
        .collect(Collectors.toList());
  }

  @Override
  @Transactional(readOnly = true)
  public PendingConnectionsResponseDto getById(UUID id) {
    PendingConnectionsEntity entity = mustFind(repository, id, "PendingConnections");
    return mapper.toResponse(entity);
  }

  @Override
  @Transactional
  public PendingConnectionsResponseDto create(PendingConnectionsRequestDto request) {
    PendingConnectionsEntity entity = mapper.toEntity(request);
    return mapper.toResponse(repository.save(entity));
  }

  @Override
  @Transactional
  public PendingConnectionsResponseDto update(UUID id, PendingConnectionsRequestDto request) {
    PendingConnectionsEntity entity = mustFind(repository, id, "PendingConnections");
    mapper.update(request, entity);
    return mapper.toResponse(repository.save(entity));
  }

  @Override
  @Transactional
  public void delete(UUID id) {
    PendingConnectionsEntity entity = mustFind(repository, id, "PendingConnections");
    entity.setIsDeleted(true);
    repository.save(entity);
  }
}
