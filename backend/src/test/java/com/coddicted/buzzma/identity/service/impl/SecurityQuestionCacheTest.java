package com.coddicted.buzzma.identity.service.impl;

import static com.coddicted.buzzma.identity.service.impl.Fixtures.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.coddicted.buzzma.config.CacheConfig;
import com.coddicted.buzzma.identity.entity.SecurityQuestion;
import com.coddicted.buzzma.identity.persistence.SecurityAnswerRepository;
import com.coddicted.buzzma.identity.persistence.SecurityQuestionRepository;
import com.coddicted.buzzma.identity.service.SecurityQuestionAnswerService;
import com.coddicted.buzzma.shared.common.PasswordService;
import com.coddicted.buzzma.shared.constants.WellKnownCaches;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

@SpringJUnitConfig
class SecurityQuestionCacheTest {

  @Autowired private SecurityQuestionAnswerService service;
  @Autowired private SecurityQuestionRepository mockQuestionRepository;
  @Autowired private CacheManager cacheManager;

  @BeforeEach
  void setUp() {
    reset(this.mockQuestionRepository);
    this.cacheManager.getCache(WellKnownCaches.SECURITY_QUESTIONS_CACHE).clear();
  }

  @Test
  void testListSecurityQuestionsSecondCallHitsCacheNotRepository() {
    final List<SecurityQuestion> questions = List.of(SECURITY_QUESTION_2);
    when(this.mockQuestionRepository.findAllByIsDeletedFalse()).thenReturn(questions);

    this.service.listSecurityQuestions();
    this.service.listSecurityQuestions();

    verify(this.mockQuestionRepository, times(1)).findAllByIsDeletedFalse();
  }

  @Test
  void testListSecurityQuestionsCacheEvictedAfterCreate() {
    final List<SecurityQuestion> questions = List.of(SECURITY_QUESTION_2);
    when(this.mockQuestionRepository.findAllByIsDeletedFalse()).thenReturn(questions);
    when(this.mockQuestionRepository.save(SECURITY_QUESTION_1))
        .thenReturn(EXPECTED_SECURITY_QUESTION_1);

    this.service.listSecurityQuestions();
    this.service.createSecurityQuestion(SECURITY_QUESTION_1);
    this.service.listSecurityQuestions();

    verify(this.mockQuestionRepository, times(2)).findAllByIsDeletedFalse();
  }

  @Test
  void testListSecurityQuestionsCacheEvictedAfterDelete() {
    final List<SecurityQuestion> questions = List.of(SECURITY_QUESTION_2);
    when(this.mockQuestionRepository.findAllByIsDeletedFalse()).thenReturn(questions);
    when(this.mockQuestionRepository.findById(QUESTION_ID))
        .thenReturn(Optional.of(SECURITY_QUESTION_2));
    when(this.mockQuestionRepository.save(org.mockito.ArgumentMatchers.any()))
        .thenReturn(SECURITY_QUESTION_2);

    this.service.listSecurityQuestions();
    this.service.deleteSecurityQuestion(QUESTION_ID, REQUESTER_ID);
    this.service.listSecurityQuestions();

    verify(this.mockQuestionRepository, times(2)).findAllByIsDeletedFalse();
  }

  @Configuration
  @Import(CacheConfig.class)
  static class TestConfig {

    @Bean
    SecurityQuestionRepository securityQuestionRepository() {
      return mock(SecurityQuestionRepository.class);
    }

    @Bean
    SecurityAnswerRepository securityAnswerRepository() {
      return mock(SecurityAnswerRepository.class);
    }

    @Bean
    PasswordService passwordService() {
      return mock(PasswordService.class);
    }

    @Bean
    SecurityQuestionAnswerServiceImpl securityQuestionAnswerService(
        final SecurityQuestionRepository questionRepository,
        final SecurityAnswerRepository answerRepository,
        final PasswordService passwordService) {
      return new SecurityQuestionAnswerServiceImpl(
          questionRepository, answerRepository, passwordService);
    }
  }
}
