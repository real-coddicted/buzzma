package com.coddicted.buzzma.campaign.service.impl;

import com.coddicted.buzzma.campaign.api.DealRequestDto;
import com.coddicted.buzzma.campaign.api.DealResponseDto;
import com.coddicted.buzzma.campaign.mapper.DealMapper;
import com.coddicted.buzzma.campaign.entity.Deal;
import com.coddicted.buzzma.campaign.persistence.DealRepository;
import com.coddicted.buzzma.campaign.service.DealService;
import com.coddicted.buzzma.shared.common.OffsetBasedPageRequest;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DealServiceImpl extends BaseCrudService implements DealService {

  private final DealRepository repository;
  private final DealMapper mapper;

  public DealServiceImpl(DealRepository repository, DealMapper mapper) {
    this.repository = repository;
    this.mapper = mapper;
  }

  @Override
  @Transactional(readOnly = true)
  public List<DealResponseDto> list(int limit, int offset) {
    var pageable =
        new OffsetBasedPageRequest(limit, offset, Sort.by(Sort.Direction.DESC, "createdAt"));
    return repository.findAllByIsDeletedFalse(pageable).stream()
        .map(mapper::toResponse)
        .collect(Collectors.toList());
  }

  @Override
  @Transactional(readOnly = true)
  public DealResponseDto getById(UUID id) {
    Deal entity = mustFind(repository, id, "Deals");
    return mapper.toResponse(entity);
  }

  @Override
  @Transactional
  public DealResponseDto create(DealRequestDto request) {
    Deal entity = mapper.toEntity(request);
    return mapper.toResponse(repository.save(entity));
  }

  @Override
  @Transactional
  public DealResponseDto update(UUID id, DealRequestDto request) {
    Deal entity = mustFind(repository, id, "Deals");
    mapper.update(request, entity);
    return mapper.toResponse(repository.save(entity));
  }

  @Override
  @Transactional
  public void delete(UUID id) {
    Deal entity = mustFind(repository, id, "Deals");
    entity.setIsDeleted(true);
    repository.save(entity);
  }
}
