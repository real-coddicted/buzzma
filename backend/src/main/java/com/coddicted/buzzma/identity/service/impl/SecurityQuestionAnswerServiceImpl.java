package com.coddicted.buzzma.identity.service.impl;

import com.coddicted.buzzma.identity.entity.SecurityAnswer;
import com.coddicted.buzzma.identity.entity.SecurityQuestion;
import com.coddicted.buzzma.identity.entity.SecurityQuestionWrapper;
import com.coddicted.buzzma.identity.persistence.SecurityAnswerRepository;
import com.coddicted.buzzma.identity.persistence.SecurityQuestionRepository;
import com.coddicted.buzzma.identity.service.SecurityQuestionAnswerService;
import com.coddicted.buzzma.shared.common.BaseCrudService;
import com.coddicted.buzzma.shared.common.PasswordService;
import com.coddicted.buzzma.shared.constants.WellKnownCaches;
import com.coddicted.buzzma.shared.exception.NotFoundException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SecurityQuestionAnswerServiceImpl extends BaseCrudService
    implements SecurityQuestionAnswerService {

  private final SecurityQuestionRepository questionRepository;
  private final SecurityAnswerRepository answerRepository;
  private final PasswordService passwordService;

  public SecurityQuestionAnswerServiceImpl(
      final SecurityQuestionRepository questionRepository,
      final SecurityAnswerRepository answerRepository,
      final PasswordService passwordService) {
    this.questionRepository = questionRepository;
    this.answerRepository = answerRepository;
    this.passwordService = passwordService;
  }

  @Override
  @Cacheable(WellKnownCaches.SECURITY_QUESTIONS_CACHE)
  public List<SecurityQuestion> listSecurityQuestions() {
    return this.questionRepository.findAllByIsDeletedFalse();
  }

  @Override
  @Transactional
  @CacheEvict(cacheNames = WellKnownCaches.SECURITY_QUESTIONS_CACHE, allEntries = true)
  public SecurityQuestion createSecurityQuestion(final SecurityQuestion securityQuestion) {
    return this.questionRepository.save(securityQuestion);
  }

  @Override
  @Transactional
  @CacheEvict(cacheNames = WellKnownCaches.SECURITY_QUESTIONS_CACHE, allEntries = true)
  public SecurityQuestion updateSecurityQuestion(final SecurityQuestion securityQuestion) {
    return this.questionRepository.save(securityQuestion);
  }

  @Override
  @Transactional
  @CacheEvict(cacheNames = WellKnownCaches.SECURITY_QUESTIONS_CACHE, allEntries = true)
  public SecurityQuestion deleteSecurityQuestion(final UUID id, final UUID requesterId) {
    final SecurityQuestion question = mustFind(this.questionRepository, id, "SecurityQuestion");
    return this.questionRepository.save(
        question.toBuilder().isDeleted(true).updatedBy(requesterId).build());
  }

  @Override
  @Transactional
  public SecurityAnswer createSecurityAnswer(final SecurityAnswer securityAnswer) {
    return this.answerRepository.save(
        securityAnswer.toBuilder()
            .answerHash(this.passwordService.hashPassword(securityAnswer.getAnswerHash()))
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build());
  }

  @Override
  @Transactional
  public List<SecurityAnswer> updateSecurityAnswers(final List<SecurityAnswer> securityAnswers) {
    final List<SecurityAnswer> hashed =
        securityAnswers.stream()
            .map(
                a ->
                    a.toBuilder()
                        .answerHash(this.passwordService.hashPassword(a.getAnswerHash()))
                        .build())
            .toList();
    return this.answerRepository.saveAll(hashed);
  }

  @Override
  public List<SecurityQuestionWrapper> getSecurityQuestionsByUserId(final UUID userId) {
    return this.answerRepository.findSecurityQuestionByUserId(userId);
  }

  @Override
  public boolean verifySecurityAnswer(final SecurityAnswer securityAnswer) {
    final SecurityAnswer stored =
        this.answerRepository
            .findByUserIdAndQuestionId(securityAnswer.getUserId(), securityAnswer.getQuestionId())
            .orElseThrow(() -> new NotFoundException("Security answer not found"));
    return this.passwordService.verifyPassword(
        securityAnswer.getAnswerHash(), stored.getAnswerHash());
  }
}
