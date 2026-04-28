package com.coddicted.buzzma.mediator.service.impl;

import com.coddicted.buzzma.mediator.api.MediatorProfilesRequestDto;
import com.coddicted.buzzma.mediator.api.MediatorProfilesResponseDto;
import com.coddicted.buzzma.mediator.mapper.MediatorProfilesMapper;
import com.coddicted.buzzma.mediator.persistence.MediatorProfilesEntity;
import com.coddicted.buzzma.mediator.persistence.MediatorProfilesRepository;
import com.coddicted.buzzma.mediator.service.MediatorProfileService;
import com.coddicted.buzzma.shared.common.OffsetBasedPageRequest;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MediatorProfileServiceImpl extends BaseCrudService implements MediatorProfileService {

  private final MediatorProfilesRepository repository;
  private final MediatorProfilesMapper mapper;

  public MediatorProfileServiceImpl(
      MediatorProfilesRepository repository, MediatorProfilesMapper mapper) {
    this.repository = repository;
    this.mapper = mapper;
  }

  @Override
  @Transactional(readOnly = true)
  public List<MediatorProfilesResponseDto> list(int limit, int offset) {
    var pageable =
        new OffsetBasedPageRequest(limit, offset, Sort.by(Sort.Direction.DESC, "createdAt"));
    return repository.findAllByIsDeletedFalse(pageable).stream()
        .map(mapper::toResponse)
        .collect(Collectors.toList());
  }

  @Override
  @Transactional(readOnly = true)
  public MediatorProfilesResponseDto getById(UUID id) {
    MediatorProfilesEntity entity = mustFind(repository, id, "MediatorProfiles");
    return mapper.toResponse(entity);
  }

  @Override
  @Transactional
  public MediatorProfilesResponseDto create(MediatorProfilesRequestDto request) {
    MediatorProfilesEntity entity = mapper.toEntity(request);
    return mapper.toResponse(repository.save(entity));
  }

  @Override
  @Transactional
  public MediatorProfilesResponseDto update(UUID id, MediatorProfilesRequestDto request) {
    MediatorProfilesEntity entity = mustFind(repository, id, "MediatorProfiles");
    mapper.update(request, entity);
    return mapper.toResponse(repository.save(entity));
  }

  @Override
  @Transactional
  public void delete(UUID id) {
    MediatorProfilesEntity entity = mustFind(repository, id, "MediatorProfiles");
    entity.setIsDeleted(true);
    repository.save(entity);
  }
}
