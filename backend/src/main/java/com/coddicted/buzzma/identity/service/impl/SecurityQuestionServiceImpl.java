package com.coddicted.buzzma.identity.service.impl;

import com.coddicted.buzzma.identity.api.SecurityQuestionsRequestDto;
import com.coddicted.buzzma.identity.api.SecurityQuestionsResponseDto;
import com.coddicted.buzzma.identity.mapper.SecurityQuestionsMapper;
import com.coddicted.buzzma.identity.entity.SecurityQuestionsEntity;
import com.coddicted.buzzma.identity.persistence.SecurityQuestionRepository;
import com.coddicted.buzzma.identity.service.SecurityQuestionService;
import com.coddicted.buzzma.shared.common.OffsetBasedPageRequest;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SecurityQuestionServiceImpl extends BaseCrudService
    implements SecurityQuestionService {

  private final SecurityQuestionRepository repository;
  private final SecurityQuestionsMapper mapper;

  public SecurityQuestionServiceImpl(
          SecurityQuestionRepository repository, SecurityQuestionsMapper mapper) {
    this.repository = repository;
    this.mapper = mapper;
  }

  @Override
  @Transactional(readOnly = true)
  public List<SecurityQuestionsResponseDto> list(int limit, int offset) {
    var pageable =
        new OffsetBasedPageRequest(limit, offset, Sort.by(Sort.Direction.DESC, "createdAt"));
    return repository.findAll(pageable).stream()
        .map(mapper::toResponse)
        .collect(Collectors.toList());
  }

  @Override
  @Transactional(readOnly = true)
  public SecurityQuestionsResponseDto getById(UUID id) {
    SecurityQuestionsEntity entity = mustFind(repository, id, "SecurityQuestions");
    return mapper.toResponse(entity);
  }

  @Override
  @Transactional
  public SecurityQuestionsResponseDto create(SecurityQuestionsRequestDto request) {
    SecurityQuestionsEntity entity = mapper.toEntity(request);
    return mapper.toResponse(repository.save(entity));
  }

  @Override
  @Transactional
  public SecurityQuestionsResponseDto update(UUID id, SecurityQuestionsRequestDto request) {
    SecurityQuestionsEntity entity = mustFind(repository, id, "SecurityQuestions");
    mapper.update(request, entity);
    return mapper.toResponse(repository.save(entity));
  }

  @Override
  @Transactional
  public void delete(UUID id) {
    repository.deleteById(id);
  }
}
