package com.coddicted.buzzma.identity.service.impl;

import com.coddicted.buzzma.identity.api.InvitesRequestDto;
import com.coddicted.buzzma.identity.api.InvitesResponseDto;
import com.coddicted.buzzma.identity.entity.InviteEntity;
import com.coddicted.buzzma.identity.mapper.InvitesMapper;
import com.coddicted.buzzma.identity.persistence.InviteRepository;
import com.coddicted.buzzma.identity.service.InviteService;
import com.coddicted.buzzma.shared.common.OffsetBasedPageRequest;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InviteServiceImpl implements InviteService {

  private final InviteRepository repository;
  private final InvitesMapper mapper;

  public InviteServiceImpl(InviteRepository repository, InvitesMapper mapper) {
    this.repository = repository;
    this.mapper = mapper;
  }

  @Override
  @Transactional(readOnly = true)
  public List<InvitesResponseDto> list(int limit, int offset) {
    var pageable =
        new OffsetBasedPageRequest(limit, offset, Sort.by(Sort.Direction.DESC, "createdAt"));
    return repository.findAll(pageable).stream()
        .map(mapper::toResponse)
        .collect(Collectors.toList());
  }

  @Override
  @Transactional(readOnly = true)
  public InvitesResponseDto getById(UUID id) {
    InviteEntity entity = mustFind(repository, id, "Invites");
    return mapper.toResponse(entity);
  }

  @Override
  @Transactional
  public InvitesResponseDto create(InvitesRequestDto request) {
    InviteEntity entity = mapper.toEntity(request);
    return mapper.toResponse(repository.save(entity));
  }

  @Override
  @Transactional
  public InvitesResponseDto update(UUID id, InvitesRequestDto request) {
    InviteEntity entity = mustFind(repository, id, "Invites");
    mapper.update(request, entity);
    return mapper.toResponse(repository.save(entity));
  }

  @Override
  @Transactional
  public void delete(UUID id) {
    repository.deleteById(id);
  }
}
